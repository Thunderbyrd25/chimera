package com.chimera.gene;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// The two starter traits that can't be expressed as attribute modifiers (see CLAUDE.md:
// "prefer attribute modifiers wherever a trait can be expressed as one"). Bovine Vigor and the
// armor half of Thick Fleece are plain attribute modifiers handled entirely by Curios'
// getAttributeModifiers (see com.chimera.curios.ChimeraCuriosCompat) - nothing needed here.
public class GeneEffectHandlers {

    private static final float RUMINANT_GUT_REDUCTION = 0.3F;

    // Not persisted - just a per-tick smoothing cache, safe to lose on reload/logout.
    private static final Map<UUID, Float> lastExhaustion = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }

        if (PlayerGeneEffects.hasBehavior(player, "ruminant_gut")) {
            dampenExhaustion(player);
        } else {
            lastExhaustion.remove(player.getUUID());
        }

        if (PlayerGeneEffects.hasBehavior(player, "freeze_immunity")) {
            if (player.getTicksFrozen() > 0) {
                player.setTicksFrozen(0);
            }
        }
    }

    private void dampenExhaustion(Player player) {
        FoodData foodData = player.getFoodData();
        float current = foodData.getExhaustionLevel();
        Float previous = lastExhaustion.get(player.getUUID());
        if (previous != null) {
            float delta = current - previous;
            if (delta > 0) {
                foodData.setExhaustion(previous + delta * (1.0F - RUMINANT_GUT_REDUCTION));
                current = foodData.getExhaustionLevel();
            }
        }
        lastExhaustion.put(player.getUUID(), current);
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && PlayerGeneEffects.hasBehavior(player, "hollow_bones")) {
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.5F);
        }
    }
}
