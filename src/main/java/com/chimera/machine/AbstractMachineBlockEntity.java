package com.chimera.machine;

import com.chimera.ChimeraItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

// Shared base for all six machines: item storage, a tick-driven progress counter, save/load,
// menu sync, fuel, and upgrade slots. Concrete machines configure this via slot count and
// canProcess()/process() rather than reimplementing the plumbing.
// See CLAUDE.md: "Build one abstract machine base class first."
public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MAX_UPGRADE_SLOTS = 3;

    protected final ItemStackHandler inventory;
    protected final ItemStackHandler fuelInventory;
    protected final ItemStackHandler upgradeInventory;
    protected int progress = 0;
    protected int maxProgress;
    protected int upgradeSlotCount = 1;
    // Server: overwritten with the live effectiveMaxProgress() every tick. Client: overwritten
    // by incoming ContainerData sync. Either way, get(1) below just returns this.
    private int syncedMaxProgress;

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount, int maxProgress) {
        super(type, pos, state);
        this.maxProgress = maxProgress;
        this.syncedMaxProgress = maxProgress;
        this.inventory = new ItemStackHandler(slotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.fuelInventory = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.upgradeInventory = new ItemStackHandler(MAX_UPGRADE_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStackHandler getFuelInventory() {
        return fuelInventory;
    }

    public ItemStackHandler getUpgradeInventory() {
        return upgradeInventory;
    }

    public int getUpgradeSlotCount() {
        return upgradeSlotCount;
    }

    // Called from AbstractMachineBlock#useItemOn. Returns false (kit not consumed) once already
    // at the cap, so right-clicking a maxed machine with a kit just opens the menu instead.
    public boolean tryApplyUpgradeKit() {
        if (upgradeSlotCount >= MAX_UPGRADE_SLOTS) {
            return false;
        }
        upgradeSlotCount++;
        setChanged();
        return true;
    }

    protected abstract boolean canProcess();

    protected abstract void process();

    protected boolean hasFuel() {
        return fuelInventory.getStackInSlot(0).is(ChimeraItems.BIOMASS.get());
    }

    // Multiplies together across every installed Speed Upgrade (any tier, any of the unlocked
    // slots) - identical behavior on all six machines, no per-machine code needed.
    protected float speedMultiplier() {
        float multiplier = 1.0F;
        for (int i = 0; i < upgradeSlotCount; i++) {
            ItemStack upgrade = upgradeInventory.getStackInSlot(i);
            if (upgrade.is(ChimeraItems.SPEED_UPGRADE_1.get())) {
                multiplier *= 0.9F;
            } else if (upgrade.is(ChimeraItems.SPEED_UPGRADE_2.get())) {
                multiplier *= 0.8F;
            } else if (upgrade.is(ChimeraItems.SPEED_UPGRADE_3.get())) {
                multiplier *= 0.65F;
            }
        }
        return multiplier;
    }

    public int effectiveMaxProgress() {
        return Math.max(1, Math.round(maxProgress * speedMultiplier()));
    }

    // Sums installed Yield Upgrades into a bias only GeneSequencerBlockEntity's rollByproduct()
    // reads - every other machine is fully deterministic today, so this is otherwise unused.
    protected int yieldBias() {
        int bias = 0;
        for (int i = 0; i < upgradeSlotCount; i++) {
            ItemStack upgrade = upgradeInventory.getStackInSlot(i);
            if (upgrade.is(ChimeraItems.YIELD_UPGRADE_1.get())) {
                bias += 5;
            } else if (upgrade.is(ChimeraItems.YIELD_UPGRADE_2.get())) {
                bias += 10;
            } else if (upgrade.is(ChimeraItems.YIELD_UPGRADE_3.get())) {
                bias += 20;
            }
        }
        return bias;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractMachineBlockEntity machine) {
        if (level.isClientSide) {
            return;
        }

        machine.syncedMaxProgress = machine.effectiveMaxProgress();

        if (machine.canProcess() && machine.hasFuel()) {
            machine.progress++;
            if (machine.progress >= machine.syncedMaxProgress) {
                machine.progress = 0;
                machine.fuelInventory.extractItem(0, 1, false);
                machine.process();
            }
            machine.setChanged();
        } else if (machine.progress != 0) {
            machine.progress = 0;
            machine.setChanged();
        }
    }

    // Base progress/maxProgress sync (indices 0, 1). Machines with extra state to sync
    // should override and add their own indices after these two.
    protected ContainerData createContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> syncedMaxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> syncedMaxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("FuelInventory", fuelInventory.serializeNBT(registries));
        tag.put("UpgradeInventory", upgradeInventory.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putInt("UpgradeSlotCount", upgradeSlotCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        fuelInventory.deserializeNBT(registries, tag.getCompound("FuelInventory"));
        upgradeInventory.deserializeNBT(registries, tag.getCompound("UpgradeInventory"));
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
        upgradeSlotCount = Math.max(1, Math.min(MAX_UPGRADE_SLOTS, tag.getInt("UpgradeSlotCount")));
        syncedMaxProgress = maxProgress;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }
}
