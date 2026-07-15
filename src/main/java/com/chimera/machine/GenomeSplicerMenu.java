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

public class GenomeSplicerMenu extends AbstractContainerMenu {

    // Slot box positions, shared with GenomeSplicerScreen so the drawn boxes can never drift
    // out of sync with the real slots (see SpliceCoreScreen's comment for the bug this avoids).
    // Shifted left so the rightmost genome slot clears the progress bar (x=76) with real margin.
    public static final int GENOME_SLOT_START_X = 12;
    public static final int GENOME_SLOT_SPACING = 18;
    public static final int GENOME_SLOT_Y = 17;

    // Frame + fuel sit with real space between them (not touching), each aligned under one of
    // the genome slots (frame under slot 1, fuel under slot 3) so the pair reads as centered
    // under the genome row rather than off to one side. Decoupled from the upgrade rail.
    static final int FRAME_X = GENOME_SLOT_START_X;
    static final int FUEL_X = GENOME_SLOT_START_X + 2 * GENOME_SLOT_SPACING;
    static final int FUEL_Y = 47;

    private static final int MAIN_SLOT_COUNT = GenomeSplicerBlockEntity.SLOT_OUTPUT + 1;

    private final int fuelSlot;
    private final int upgradeSlotCount;
    private final int slotCount;
    private final int playerInvStart;
    private final int playerInvEnd;
    private final int hotbarEnd;

    private final GenomeSplicerBlockEntity blockEntity;
    private final ContainerData data;

    public GenomeSplicerMenu(int containerId, Inventory playerInventory, GenomeSplicerBlockEntity blockEntity, int upgradeSlotCount) {
        super(ChimeraMenus.GENOME_SPLICER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.createContainerData();

        this.upgradeSlotCount = upgradeSlotCount;
        this.fuelSlot = MAIN_SLOT_COUNT;
        this.slotCount = MAIN_SLOT_COUNT + 1 + upgradeSlotCount;
        this.playerInvStart = slotCount;
        this.playerInvEnd = slotCount + 27;
        this.hotbarEnd = playerInvEnd + 9;

        for (int i = 0; i < GenomeSplicerBlockEntity.GENOME_SLOT_COUNT; i++) {
            addSlot(new SlotItemHandler(blockEntity.getInventory(), GenomeSplicerBlockEntity.SLOT_GENOME_START + i,
                    GENOME_SLOT_START_X + i * GENOME_SLOT_SPACING, GENOME_SLOT_Y));
        }
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GenomeSplicerBlockEntity.SLOT_FRAME, FRAME_X, FUEL_Y));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GenomeSplicerBlockEntity.SLOT_OUTPUT, 116, 32));

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
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ChimeraBlocks.GENOME_SPLICER.get());
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
            if (!this.moveItemStackTo(slotStack, GenomeSplicerBlockEntity.SLOT_GENOME_START,
                    GenomeSplicerBlockEntity.SLOT_GENOME_START + GenomeSplicerBlockEntity.GENOME_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.BLANK_GENE_CASSETTE.get())) {
            if (!this.moveItemStackTo(slotStack, GenomeSplicerBlockEntity.SLOT_FRAME, GenomeSplicerBlockEntity.SLOT_FRAME + 1, false)) {
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
