package com.chimera.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

// Shared base for the three v0.1 machines (Sequencer, Analyzer, Extractor): item storage,
// a tick-driven progress counter, save/load, and menu sync. Concrete machines configure this
// via slot count and canProcess()/process() rather than reimplementing the plumbing.
// See CLAUDE.md: "Build one abstract machine base class first."
public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider {

    protected final ItemStackHandler inventory;
    protected int progress = 0;
    protected int maxProgress;

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount, int maxProgress) {
        super(type, pos, state);
        this.maxProgress = maxProgress;
        this.inventory = new ItemStackHandler(slotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    protected abstract boolean canProcess();

    protected abstract void process();

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractMachineBlockEntity machine) {
        if (level.isClientSide) {
            return;
        }

        if (machine.canProcess()) {
            machine.progress++;
            if (machine.progress >= machine.maxProgress) {
                machine.progress = 0;
                machine.process();
            }
            machine.setChanged();
        } else if (machine.progress != 0) {
            machine.progress = 0;
            machine.setChanged();
        }
    }

    // Base progress/maxProgress sync (indices 0, 1). Machines with extra state to sync
    // (e.g. the Sequencer's fuel) should override and add their own indices after these two.
    protected ContainerData createContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
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
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }
}
