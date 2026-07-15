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

public class CentrifugeMenu extends AbstractContainerMenu {

    // Genome + frame sit with real space between them (not touching), shifted left to clear the
    // progress bar. Fuel sits below, centered on the gap between them. FUEL_Y lands in the gap
    // between the input row (ends y=34) and vanilla's "Inventory" label (starts y=72).
    static final int GENOME_X = 20;
    static final int FRAME_X = 56;
    static final int PROGRESS_X = 82;
    static final int FUEL_X = 38;
    static final int FUEL_Y = 44;

    private static final int MAIN_SLOT_COUNT = CentrifugeBlockEntity.SLOT_OUTPUT_START + CentrifugeBlockEntity.OUTPUT_SLOT_COUNT;

    private final int fuelSlot;
    private final int upgradeSlotCount;
    private final int slotCount;
    private final int playerInvStart;
    private final int playerInvEnd;
    private final int hotbarEnd;

    private final CentrifugeBlockEntity blockEntity;
    private final ContainerData data;

    public CentrifugeMenu(int containerId, Inventory playerInventory, CentrifugeBlockEntity blockEntity, int upgradeSlotCount) {
        super(ChimeraMenus.CENTRIFUGE.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.createContainerData();

        this.upgradeSlotCount = upgradeSlotCount;
        this.fuelSlot = MAIN_SLOT_COUNT;
        this.slotCount = MAIN_SLOT_COUNT + 1 + upgradeSlotCount;
        this.playerInvStart = slotCount;
        this.playerInvEnd = slotCount + 27;
        this.hotbarEnd = playerInvEnd + 9;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), CentrifugeBlockEntity.SLOT_GENOME, GENOME_X, 17));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), CentrifugeBlockEntity.SLOT_FRAME, FRAME_X, 17));
        for (int i = 0; i < CentrifugeBlockEntity.OUTPUT_SLOT_COUNT; i++) {
            addSlot(new SlotItemHandler(blockEntity.getInventory(), CentrifugeBlockEntity.SLOT_OUTPUT_START + i, 116, 10 + i * 22));
        }

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

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ChimeraBlocks.CENTRIFUGE.get());
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
            if (!this.moveItemStackTo(slotStack, CentrifugeBlockEntity.SLOT_GENOME, CentrifugeBlockEntity.SLOT_GENOME + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.BLANK_GENOME.get())) {
            if (!this.moveItemStackTo(slotStack, CentrifugeBlockEntity.SLOT_FRAME, CentrifugeBlockEntity.SLOT_FRAME + 1, false)) {
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
