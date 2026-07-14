package com.chimera.splice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.ChimeraMenus;
import com.chimera.gene.GeneInstance;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// A held-item GUI rather than a block one: the "container" is a transient SimpleContainer
// (1-3 slots depending on Mk1/Mk2/Mk3) seeded from the Splice Core's own TRAITS component,
// written back on every broadcastChanges() tick - a single catch-all interception point
// instead of chasing every Slot mutation path (set/remove/quickMove all funnel through it
// eventually, but Slot#remove(int) skips setChanged()).
public class SpliceCoreMenu extends AbstractContainerMenu {

    private final int slotCount;
    private final int cassetteSlotStartX;
    private final int playerInvStart;
    private final int playerInvEnd;
    private final int hotbarEnd;

    private final Player player;
    private final InteractionHand hand;
    private final SimpleContainer container;

    public SpliceCoreMenu(int containerId, Inventory playerInventory, InteractionHand hand, int slotCount) {
        super(ChimeraMenus.SPLICE_CORE.get(), containerId);
        this.player = playerInventory.player;
        this.hand = hand;
        this.slotCount = slotCount;
        this.playerInvStart = slotCount;
        this.playerInvEnd = slotCount + 27;
        this.hotbarEnd = playerInvEnd + 9;
        this.container = new SimpleContainer(slotCount);
        this.cassetteSlotStartX = 80 - (slotCount - 1) * 9;

        ItemStack coreStack = player.getItemInHand(hand);
        List<GeneInstance> traits = coreStack.get(ChimeraDataComponents.TRAITS.get());
        if (traits != null) {
            for (int i = 0; i < traits.size() && i < slotCount; i++) {
                ItemStack cassette = new ItemStack(ChimeraItems.GENE_CASSETTE.get());
                cassette.set(ChimeraDataComponents.TRAITS.get(), List.of(traits.get(i)));
                cassette.set(ChimeraDataComponents.INERT.get(), false);
                container.setItem(i, cassette);
            }
        }

        for (int i = 0; i < slotCount; i++) {
            addSlot(new Slot(container, i, cassetteSlotStartX + i * 18, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ChimeraItems.GENE_CASSETTE.get());
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public int getSlotCount() {
        return slotCount;
    }

    public int getCassetteSlotStartX() {
        return cassetteSlotStartX;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        syncCassettesToCore();
    }

    private void syncCassettesToCore() {
        ItemStack coreStack = player.getItemInHand(hand);
        if (!(coreStack.getItem() instanceof com.chimera.item.SpliceCoreItem)) {
            return;
        }

        List<GeneInstance> newTraits = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            ItemStack cassette = container.getItem(i);
            if (cassette.isEmpty()) {
                continue;
            }
            List<GeneInstance> cassetteTraits = cassette.get(ChimeraDataComponents.TRAITS.get());
            if (cassetteTraits != null && !cassetteTraits.isEmpty()) {
                newTraits.add(cassetteTraits.get(0));
            }
        }

        List<GeneInstance> currentTraits = coreStack.get(ChimeraDataComponents.TRAITS.get());
        if (currentTraits == null) {
            currentTraits = List.of();
        }
        if (!Objects.equals(currentTraits, newTraits)) {
            if (newTraits.isEmpty()) {
                coreStack.remove(ChimeraDataComponents.TRAITS.get());
            } else {
                coreStack.set(ChimeraDataComponents.TRAITS.get(), List.copyOf(newTraits));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).getItem() instanceof com.chimera.item.SpliceCoreItem;
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
        } else if (slotStack.is(ChimeraItems.GENE_CASSETTE.get())) {
            if (!this.moveItemStackTo(slotStack, 0, slotCount, false)) {
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
