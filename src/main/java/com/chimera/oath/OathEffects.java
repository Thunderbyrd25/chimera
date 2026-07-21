package com.chimera.oath;

import com.chimera.ChimeraAttachments;
import com.chimera.ChimeraItems;
import com.chimera.ChimeraMod;
import com.chimera.item.TheOathItem;
import com.chimera.network.OathResponsePayload;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// Biopedia+Oath work order Milestone 2. handleResponse is the server-side authority for
// accepting/declining The Oath - never trusts the client's claim alone (mirrors
// GeneEffectHandlers.handleGrassFedUse's own documented convention).
public final class OathEffects {

    private OathEffects() {}

    public static void handleResponse(Player player, OathResponsePayload payload) {
        if (player.level().isClientSide) {
            return;
        }

        ItemStack stack = player.getItemInHand(payload.hand());
        if (!(stack.getItem() instanceof TheOathItem)) {
            return;
        }

        PlayerOathData current = player.getData(ChimeraAttachments.PLAYER_OATH_DATA.get());
        if (current.hasOath()) {
            return;
        }

        if (!payload.accepted()) {
            return;
        }

        EquipmentSlot slot = payload.hand() == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.shrink(1);
        player.setItemSlot(slot, stack);

        player.setData(ChimeraAttachments.PLAYER_OATH_DATA.get(), current.withOathTaken());

        ItemStack biopedia = new ItemStack(ChimeraItems.THE_BIOPEDIA.get());
        if (!player.getInventory().add(biopedia)) {
            player.drop(biopedia, false);
        }

        ChimeraMod.LOGGER.info("[oath-debug] {} accepted the Oath: hasOath=true, path=SCIENTIST",
                player.getName().getString());
    }

    // The boon's reusable gate (Milestone 2 builds this; the actual write-hook that reads it
    // lands in Milestone 3, alongside the Biopedia that consumes DISCOVERED_GENES - see
    // NOTES.md for why that's deferred rather than solved here).
    public static boolean diligentStudyActive(Player player) {
        PlayerOathData data = player.getData(ChimeraAttachments.PLAYER_OATH_DATA.get());
        return data.hasOath() && !data.oathBroken();
    }
}
