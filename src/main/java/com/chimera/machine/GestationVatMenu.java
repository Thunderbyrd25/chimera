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

public class GestationVatMenu extends AbstractContainerMenu {

    // Genome A, Genome B, blank Egg in a row - shared with GestationVatScreen so the drawn boxes
    // can never drift out of sync with the real slots (see SpliceCoreScreen's own comment for
    // the bug this avoids).
    public static final int INPUT_SLOT_START_X = 12;
    public static final int INPUT_SLOT_SPACING = 18;
    public static final int INPUT_SLOT_Y = 17;

    // Fuel sits under the egg slot (third input), decoupled from the upgrade rail like every
    // other machine's own fuel position.
    static final int FUEL_X = INPUT_SLOT_START_X + 2 * INPUT_SLOT_SPACING;
    static final int FUEL_Y = 47;

    private static final int MAIN_SLOT_COUNT = GestationVatBlockEntity.SLOT_OUTPUT + 1;

    private final int fuelSlot;
    private final int upgradeSlotCount;
    private final int slotCount;
    private final int playerInvStart;
    private final int playerInvEnd;
    private final int hotbarEnd;

    private final GestationVatBlockEntity blockEntity;
    private final ContainerData data;

    public GestationVatMenu(int containerId, Inventory playerInventory, GestationVatBlockEntity blockEntity, int upgradeSlotCount) {
        super(ChimeraMenus.GESTATION_VAT.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.createContainerData();

        this.upgradeSlotCount = upgradeSlotCount;
        this.fuelSlot = MAIN_SLOT_COUNT;
        this.slotCount = MAIN_SLOT_COUNT + 1 + upgradeSlotCount;
        this.playerInvStart = slotCount;
        this.playerInvEnd = slotCount + 27;
        this.hotbarEnd = playerInvEnd + 9;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), GestationVatBlockEntity.SLOT_GENOME_A, INPUT_SLOT_START_X, INPUT_SLOT_Y));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GestationVatBlockEntity.SLOT_GENOME_B,
                INPUT_SLOT_START_X + INPUT_SLOT_SPACING, INPUT_SLOT_Y));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GestationVatBlockEntity.SLOT_EGG_BLANK,
                INPUT_SLOT_START_X + 2 * INPUT_SLOT_SPACING, INPUT_SLOT_Y));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), GestationVatBlockEntity.SLOT_OUTPUT, 116, 17));

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
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ChimeraBlocks.GESTATION_VAT.get());
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
            if (!this.moveItemStackTo(slotStack, GestationVatBlockEntity.SLOT_GENOME_A, GestationVatBlockEntity.SLOT_GENOME_B + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotStack.is(ChimeraItems.DNA_EGG.get())) {
            if (!this.moveItemStackTo(slotStack, GestationVatBlockEntity.SLOT_EGG_BLANK, GestationVatBlockEntity.SLOT_EGG_BLANK + 1, false)) {
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

        slot.onTake(player, result);
        return result;
    }
}
