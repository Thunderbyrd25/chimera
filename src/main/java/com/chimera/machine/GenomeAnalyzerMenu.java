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

    private static final int SLOT_COUNT = 2;
    private static final int PLAYER_INV_START = SLOT_COUNT;
    private static final int PLAYER_INV_END = SLOT_COUNT + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final GenomeAnalyzerBlockEntity blockEntity;
    private final ContainerData data;

    public GenomeAnalyzerMenu(int containerId, Inventory playerInventory, GenomeAnalyzerBlockEntity blockEntity) {
        super(ChimeraMenus.GENOME_ANALYZER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.createContainerData();

        addSlot(new SlotItemHandler(blockEntity.getInventory(), GenomeAnalyzerBlockEntity.SLOT_INPUT, 44, 32));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GenomeAnalyzerBlockEntity.SLOT_OUTPUT, 116, 32));

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

        if (index < SLOT_COUNT) {
            if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.SEQUENCED_GENOME.get())) {
            if (!this.moveItemStackTo(slotStack, GenomeAnalyzerBlockEntity.SLOT_INPUT, GenomeAnalyzerBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_INV_END) {
            if (!this.moveItemStackTo(slotStack, PLAYER_INV_END, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_INV_END, false)) {
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
