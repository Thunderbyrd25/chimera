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

public class BioreactorMenu extends AbstractContainerMenu {

    // The 4 byproduct inputs sit in a single evenly-spaced row instead of a cramped 2x2 grid,
    // leaving real room for the fuel slot below without touching vanilla's "Inventory" label
    // (starts y=72, AbstractContainerScreen's default imageHeight-94).
    static final int INPUT_START_X = 10;
    static final int INPUT_SPACING = 18;
    static final int PROGRESS_X = 86;
    static final int FUEL_X = 38;
    static final int FUEL_Y = 44;

    private static final int MAIN_SLOT_COUNT = BioreactorBlockEntity.SLOT_OUTPUT + 1;

    private final int fuelSlot;
    private final int upgradeSlotCount;
    private final int slotCount;
    private final int playerInvStart;
    private final int playerInvEnd;
    private final int hotbarEnd;

    private final BioreactorBlockEntity blockEntity;
    private final ContainerData data;

    public BioreactorMenu(int containerId, Inventory playerInventory, BioreactorBlockEntity blockEntity, int upgradeSlotCount) {
        super(ChimeraMenus.BIOREACTOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.createContainerData();

        this.upgradeSlotCount = upgradeSlotCount;
        this.fuelSlot = MAIN_SLOT_COUNT;
        this.slotCount = MAIN_SLOT_COUNT + 1 + upgradeSlotCount;
        this.playerInvStart = slotCount;
        this.playerInvEnd = slotCount + 27;
        this.hotbarEnd = playerInvEnd + 9;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), BioreactorBlockEntity.SLOT_CELL_CULTURE, INPUT_START_X, 17));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), BioreactorBlockEntity.SLOT_NUCLEOTIDE_SLURRY, INPUT_START_X + INPUT_SPACING, 17));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), BioreactorBlockEntity.SLOT_CHROMATIN_STRAND, INPUT_START_X + 2 * INPUT_SPACING, 17));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), BioreactorBlockEntity.SLOT_MUTAGEN, INPUT_START_X + 3 * INPUT_SPACING, 17));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), BioreactorBlockEntity.SLOT_OUTPUT, 116, 17));

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
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ChimeraBlocks.BIOREACTOR.get());
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
        } else if (slotStack.is(ChimeraItems.CELL_CULTURE.get())) {
            if (!this.moveItemStackTo(slotStack, BioreactorBlockEntity.SLOT_CELL_CULTURE, BioreactorBlockEntity.SLOT_CELL_CULTURE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.NUCLEOTIDE_SLURRY.get())) {
            if (!this.moveItemStackTo(slotStack, BioreactorBlockEntity.SLOT_NUCLEOTIDE_SLURRY, BioreactorBlockEntity.SLOT_NUCLEOTIDE_SLURRY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.CHROMATIN_STRAND.get())) {
            if (!this.moveItemStackTo(slotStack, BioreactorBlockEntity.SLOT_CHROMATIN_STRAND, BioreactorBlockEntity.SLOT_CHROMATIN_STRAND + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.MUTAGEN.get())) {
            if (!this.moveItemStackTo(slotStack, BioreactorBlockEntity.SLOT_MUTAGEN, BioreactorBlockEntity.SLOT_MUTAGEN + 1, false)) {
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
