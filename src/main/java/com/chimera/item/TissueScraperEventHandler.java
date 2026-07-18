package com.chimera.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// Some vanilla mobs override mobInteract() with their own item-then-fallback logic (notably
// AbstractHorse, which tries the held item and then falls through to mounting) - in practice this
// didn't reliably let TissueScraperItem win before the mount fallback ran (see NOTES.md). Hooking
// PlayerInteractEvent.EntityInteract directly runs before any of that per-entity vanilla logic, so
// we try the scraper ourselves here and cancel outright on success - vanilla's own dispatch (and
// whatever entity-specific fallback it would have run) never gets a chance to fire.
public class TissueScraperEventHandler {

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof TissueScraperItem) || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        InteractionResult result = stack.interactLivingEntity(player, target, hand);
        if (result.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
