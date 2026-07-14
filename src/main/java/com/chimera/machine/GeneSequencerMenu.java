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

public class GeneSequencerMenu extends AbstractContainerMenu {

    private static final int SLOT_COUNT = 4;
    private static final int PLAYER_INV_START = SLOT_COUNT;
    private static final int PLAYER_INV_END = SLOT_COUNT + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final GeneSequencerBlockEntity blockEntity;
    private final ContainerData data;

    public GeneSequencerMenu(int containerId, Inventory playerInventory, GeneSequencerBlockEntity blockEntity) {
        super(ChimeraMenus.GENE_SEQUENCER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.createContainerData();

        addSlot(new SlotItemHandler(blockEntity.getInventory(), GeneSequencerBlockEntity.SLOT_INPUT, 44, 35));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GeneSequencerBlockEntity.SLOT_FUEL, 44, 65));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GeneSequencerBlockEntity.SLOT_OUTPUT, 116, 35));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GeneSequencerBlockEntity.SLOT_BYPRODUCT, 116, 65));

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

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ChimeraBlocks.GENE_SEQUENCER.get());
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
        } else if (slotStack.is(ChimeraItems.TISSUE_SAMPLE.get())) {
            if (!this.moveItemStackTo(slotStack, GeneSequencerBlockEntity.SLOT_INPUT, GeneSequencerBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.NUTRIENT_AGAR.get())) {
            if (!this.moveItemStackTo(slotStack, GeneSequencerBlockEntity.SLOT_FUEL, GeneSequencerBlockEntity.SLOT_FUEL + 1, false)) {
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
