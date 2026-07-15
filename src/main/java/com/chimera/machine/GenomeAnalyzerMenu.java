package com.chimera.machine;

import com.chimera.ChimeraBlocks;
import com.chimera.ChimeraItems;
import com.chimera.ChimeraMenus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GenomeAnalyzerMenu extends AbstractContainerMenu {

    // Below the input slot, left side - decoupled from the upgrade rail entirely. Y is centered
    // in the gap between the input slot (ends y=49) and vanilla's "Inventory" label (starts
    // y=72, AbstractContainerScreen's default imageHeight-94), so it doesn't overlap either.
    static final int FUEL_X = 44;
    static final int FUEL_Y = 52;

    private static final int MAIN_SLOT_COUNT = 2;

    private final int fuelSlot;
    private final int upgradeSlotCount;
    private final int slotCount;
    private final int playerInvStart;
    private final int playerInvEnd;
    private final int hotbarEnd;

    private final GenomeAnalyzerBlockEntity blockEntity;
    private final ContainerData data;

    public GenomeAnalyzerMenu(int containerId, Inventory playerInventory, GenomeAnalyzerBlockEntity blockEntity, int upgradeSlotCount) {
        super(ChimeraMenus.GENOME_ANALYZER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.createContainerData();

        this.upgradeSlotCount = upgradeSlotCount;
        this.fuelSlot = MAIN_SLOT_COUNT;
        this.slotCount = MAIN_SLOT_COUNT + 1 + upgradeSlotCount;
        this.playerInvStart = slotCount;
        this.playerInvEnd = slotCount + 27;
        this.hotbarEnd = playerInvEnd + 9;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), GenomeAnalyzerBlockEntity.SLOT_INPUT, 44, 32));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GenomeAnalyzerBlockEntity.SLOT_OUTPUT, 116, 32));

        addSlot(new SlotItemHandler(blockEntity.getFuelInventory(), 0, FUEL_X, FUEL_Y));
        for (int i = 0; i < upgradeSlotCount; i++) {
            addSlot(new SlotItemHandler(blockEntity.getUpgradeInventory(), i,
                    MachineScreenUtil.UPGRADE_RAIL_X, MachineScreenUtil.UPGRADE_RAIL_Y + i * MachineScreenUtil.UPGRADE_RAIL_SPACING));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return Math.max(data.get(1), 1);
    }

    public int getUpgradeSlotCount() {
        return upgradeSlotCount;
    }

    public ItemStack getInputStack() {
        return this.slots.get(GenomeAnalyzerBlockEntity.SLOT_INPUT).getItem();
    }

    public ItemStack getOutputStack() {
        return this.slots.get(GenomeAnalyzerBlockEntity.SLOT_OUTPUT).getItem();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ChimeraBlocks.GENOME_ANALYZER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return result;
        }

        ItemStack slotStack = slot.getItem();
        result = slotStack.copy();

        if (index < slotCount) {
            if (!this.moveItemStackTo(slotStack, playerInvStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.SEQUENCED_GENOME.get())) {
            if (!this.moveItemStackTo(slotStack, GenomeAnalyzerBlockEntity.SLOT_INPUT, GenomeAnalyzerBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.BIOMASS.get())) {
            if (!this.moveItemStackTo(slotStack, fuelSlot, fuelSlot + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < playerInvEnd) {
            if (!this.moveItemStackTo(slotStack, playerInvEnd, hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(slotStack, playerInvStart, playerInvEnd, false)) {
            return ItemStack.EMPTY;
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return result;
    }
}
