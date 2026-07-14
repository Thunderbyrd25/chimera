package com.chimera.splice;

import java.util.List;
import java.util.Objects;

import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.ChimeraMenus;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// A held-item GUI rather than a block one: the "container" is a transient one-slot
// SimpleContainer seeded from the Splice Core's own INSTALLED_TRAIT component, written back
// on every broadcastChanges() tick - a single catch-all interception point instead of chasing
// every Slot mutation path (set/remove/quickMove all funnel through it eventually).
public class SpliceCoreMenu extends AbstractContainerMenu {

    private static final int SLOT_COUNT = 1;
    private static final int PLAYER_INV_START = SLOT_COUNT;
    private static final int PLAYER_INV_END = SLOT_COUNT + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final Player player;
    private final InteractionHand hand;
    private final SimpleContainer container = new SimpleContainer(1);

    public SpliceCoreMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ChimeraMenus.SPLICE_CORE.get(), containerId);
        this.player = playerInventory.player;
        this.hand = hand;

        ItemStack coreStack = player.getItemInHand(hand);
        ResourceLocation trait = coreStack.get(ChimeraDataComponents.INSTALLED_TRAIT.get());
        if (trait != null) {
            ItemStack cassette = new ItemStack(ChimeraItems.GENE_CASSETTE.get());
            cassette.set(ChimeraDataComponents.TRAITS.get(), List.of(trait));
            cassette.set(ChimeraDataComponents.INERT.get(), false);
            container.setItem(0, cassette);
        }

        addSlot(new Slot(container, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ChimeraItems.GENE_CASSETTE.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        syncCassetteToCore();
    }

    private void syncCassetteToCore() {
        ItemStack coreStack = player.getItemInHand(hand);
        if (!coreStack.is(ChimeraItems.SPLICE_CORE.get())) {
            return;
        }

        ItemStack cassette = container.getItem(0);
        ResourceLocation newTrait = null;
        if (!cassette.isEmpty()) {
            List<ResourceLocation> traits = cassette.get(ChimeraDataComponents.TRAITS.get());
            if (traits != null && !traits.isEmpty()) {
                newTrait = traits.get(0);
            }
        }

        ResourceLocation currentTrait = coreStack.get(ChimeraDataComponents.INSTALLED_TRAIT.get());
        if (!Objects.equals(currentTrait, newTrait)) {
            if (newTrait == null) {
                coreStack.remove(ChimeraDataComponents.INSTALLED_TRAIT.get());
            } else {
                coreStack.set(ChimeraDataComponents.INSTALLED_TRAIT.get(), newTrait);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).is(ChimeraItems.SPLICE_CORE.get());
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
        } else if (slotStack.is(ChimeraItems.GENE_CASSETTE.get())) {
            if (!this.moveItemStackTo(slotStack, 0, SLOT_COUNT, false)) {
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
