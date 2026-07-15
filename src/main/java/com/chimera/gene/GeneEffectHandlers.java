package com.chimera.gene;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// Bovine Vigor and the armor half of Thick Fleece are plain attribute modifiers handled
// entirely by Curios' getAttributeModifiers (see com.chimera.curios.ChimeraCuriosCompat) -
// nothing needed here. Everything below is for traits that can't be expressed that way.
public class GeneEffectHandlers {

    private static final float RUMINANT_GUT_REDUCTION = 0.3F;
    private static final double RAGING_BULL_COLLISION_MARGIN = 0.2;
    private static final double RAGING_BULL_KNOCKBACK_RATIO = 0.3;

    // Not persisted - transient per-tick/per-use caches, safe to lose on reload/logout.
    private static final Map<UUID, Float> lastExhaustion = new HashMap<>();
    private static final Map<UUID, Long> grassFedCooldownEndTick = new HashMap<>();
    private static final Map<UUID, Long> ragingBullHitCooldownEndTick = new HashMap<>();

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

        if (player.isSprinting()) {
            tryRagingBull(player);
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

    // Active ability: triggered by GrassFedUsePayload (com.chimera.network), never by a tick
    // loop - the player explicitly presses the key. Standing on grass = the block at the
    // player's feet is minecraft:grass_block specifically.
    public static void handleGrassFedUse(Player player) {
        Level level = player.level();
        if (level.isClientSide) {
            return;
        }

        Optional<PlayerGeneEffects.BehaviorMatch> match = PlayerGeneEffects.findBehavior(player, "grass_fed");
        if (match.isEmpty()) {
            return;
        }

        long now = level.getGameTime();
        Long cooldownEnd = grassFedCooldownEndTick.get(player.getUUID());
        if (cooldownEnd != null && now < cooldownEnd) {
            return;
        }

        BlockPos feet = player.blockPosition().below();
        if (!level.getBlockState(feet).is(Blocks.GRASS_BLOCK)) {
            return;
        }

        PlayerGeneEffects.BehaviorMatch behaviorMatch = match.get();
        int amount = (int) Math.round(behaviorMatch.scaledValue());
        player.getFoodData().eat(amount, 0.0F);
        grassFedCooldownEndTick.put(player.getUUID(), now + behaviorMatch.effect().cooldownTicks());
    }

    // Passive: sprinting into a non-player mob deals damage + knockback, on a per-target
    // cooldown so overlapping with the same mob for several ticks doesn't hit it repeatedly.
    private void tryRagingBull(Player player) {
        Optional<PlayerGeneEffects.BehaviorMatch> match = PlayerGeneEffects.findBehavior(player, "raging_bull");
        if (match.isEmpty()) {
            return;
        }

        Level level = player.level();
        long now = level.getGameTime();

        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(RAGING_BULL_COLLISION_MARGIN),
                e -> e instanceof LivingEntity && !(e instanceof Player) && e.isAlive())) {
            LivingEntity target = (LivingEntity) entity;
            Long cooldownEnd = ragingBullHitCooldownEndTick.get(target.getUUID());
            if (cooldownEnd != null && now < cooldownEnd) {
                continue;
            }

            PlayerGeneEffects.BehaviorMatch behaviorMatch = match.get();
            float damage = (float) behaviorMatch.scaledValue();
            boolean hurt = target.hurt(level.damageSources().playerAttack(player), damage);
            if (hurt) {
                target.knockback(damage * RAGING_BULL_KNOCKBACK_RATIO,
                        Mth.sin(player.getYRot() * Mth.DEG_TO_RAD),
                        -Mth.cos(player.getYRot() * Mth.DEG_TO_RAD));
                ragingBullHitCooldownEndTick.put(target.getUUID(), now + behaviorMatch.effect().cooldownTicks());
            }
        }
    }
}
