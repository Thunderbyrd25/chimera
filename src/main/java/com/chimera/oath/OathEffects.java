package com.chimera.oath;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.chimera.ChimeraAttachments;
import com.chimera.ChimeraItems;
import com.chimera.ChimeraMod;
import com.chimera.item.TheOathItem;
import com.chimera.network.OathResponsePayload;

import net.minecraft.resources.ResourceLocation;
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

    // The boon's reusable gate, built in Milestone 2. Milestone 3 finally wires up both
    // write-hook call sites (SpliceCoreMenu for the normal path, GenomeAnalyzerMenu's output
    // slot for this boon specifically) - see discoverGenes below.
    public static boolean diligentStudyActive(Player player) {
        PlayerOathData data = player.getData(ChimeraAttachments.PLAYER_OATH_DATA.get());
        return data.hasOath() && !data.oathBroken();
    }

    // Biopedia+Oath work order Milestone 3: the shared discovery-write helper both hook sites
    // call. DISCOVERED_GENES is a Set, so unioning in already-known ids is a harmless no-op -
    // no "is this actually new" diffing needed at either call site.
    public static void discoverGenes(Player player, Collection<ResourceLocation> geneIds) {
        if (geneIds.isEmpty()) {
            return;
        }
        Set<ResourceLocation> current = player.getData(ChimeraAttachments.DISCOVERED_GENES.get());
        if (current.containsAll(geneIds)) {
            return;
        }
        Set<ResourceLocation> updated = new HashSet<>(current);
        updated.addAll(geneIds);
        player.setData(ChimeraAttachments.DISCOVERED_GENES.get(), Set.copyOf(updated));
    }
}
