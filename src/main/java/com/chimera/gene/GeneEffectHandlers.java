package com.chimera.gene;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// Bovine Vigor and the armor half of Thick Fleece are plain attribute modifiers handled
// entirely by Curios' getAttributeModifiers (see com.chimera.curios.ChimeraCuriosCompat) -
// nothing needed here. Everything below is for traits that can't be expressed that way.
public class GeneEffectHandlers {

    private static final float RUMINANT_GUT_REDUCTION = 0.3F;
    private static final double RAGING_BULL_COLLISION_MARGIN = 0.2;
    private static final double RAGING_BULL_KNOCKBACK_RATIO = 0.3;
    private static final double RAMMING_CHARGE_KNOCKBACK_RATIO = 0.3;
    private static final double RAMMING_CHARGE_STAGGER_KNOCKBACK = 0.4;
    private static final int RAMMING_CHARGE_STAGGER_SLOWNESS_TICKS = 20;
    private static final double ARACHNID_CLIMB_SPEED = 0.15;
    private static final int SUNBURN_WEAKNESS_TICKS = 60;
    private static final float SUNBURN_LIGHT_THRESHOLD = 0.5F;
    private static final double PACK_INSTINCT_RADIUS = 8.0;

    // Not persisted - transient per-tick/per-use caches, safe to lose on reload/logout.
    private static final Map<UUID, Float> lastExhaustion = new HashMap<>();
    private static final Map<UUID, Float> equineGaitLastExhaustion = new HashMap<>();
    private static final Map<UUID, Long> grassFedCooldownEndTick = new HashMap<>();
    private static final Map<UUID, Long> ragingBullHitCooldownEndTick = new HashMap<>();
    private static final Map<UUID, Long> rammingChargeHitCooldownEndTick = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }

        if (PlayerGeneEffects.hasBehavior(player, "ruminant_gut")) {
            scaleExhaustionGain(player, lastExhaustion, 1.0F - RUMINANT_GUT_REDUCTION);
        } else {
            lastExhaustion.remove(player.getUUID());
        }

        Optional<PlayerGeneEffects.BehaviorMatch> equineHunger = PlayerGeneEffects.findBehavior(player, "equine_gait_hunger");
        if (equineHunger.isPresent()) {
            scaleExhaustionGain(player, equineGaitLastExhaustion, 1.0F + (float) (equineHunger.get().scaledValue() / 100.0));
        } else {
            equineGaitLastExhaustion.remove(player.getUUID());
        }

        if (PlayerGeneEffects.hasBehavior(player, "freeze_immunity")) {
            if (player.getTicksFrozen() > 0) {
                player.setTicksFrozen(0);
            }
        }

        if (PlayerGeneEffects.hasBehavior(player, "arachnid_climb")) {
            tryWallClimb(player);
        }

        if (PlayerGeneEffects.hasBehavior(player, "undying_hunger_sunburn")) {
            tryUndyingHungerSunburn(player);
        }

        if (player.isSprinting()) {
            tryRagingBull(player);
            tryRammingCharge(player);
        }
    }

    // Spider trait: reimplements the ladder/onClimbable movement manually, since
    // LivingEntity#onClimbable() isn't overridable for Player without Mixins. Pushing against
    // any wall (horizontalCollision, airborne) grants a slow climb rather than requiring a
    // specific climbable block.
    private void tryWallClimb(Player player) {
        if (player.horizontalCollision && !player.onGround()) {
            Vec3 delta = player.getDeltaMovement();
            player.setDeltaMovement(delta.x, Math.max(delta.y, ARACHNID_CLIMB_SPEED), delta.z);
            player.resetFallDistance();
        }
    }

    // Zombie drawback: Mob#isSunBurnTick() is protected/Mob-only, so this manually replicates
    // its logic (see Mob.java) for a Player. Weakness instead of catching fire - a "nod, not a
    // groan" drawback per the work order's framing.
    private void tryUndyingHungerSunburn(Player player) {
        Level level = player.level();
        if (!level.isDay()) {
            return;
        }
        // getLightLevelDependentMagicValue() is deprecated on Entity/LevelReader but is exactly
        // what vanilla's own Mob#isSunBurnTick() calls - there's no non-deprecated equivalent.
        if (player.getLightLevelDependentMagicValue() <= SUNBURN_LIGHT_THRESHOLD) {
            return;
        }
        if (player.isInWaterRainOrBubble() || player.isInPowderSnow || player.wasInPowderSnow) {
            return;
        }
        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!level.canSeeSky(eyePos)) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SUNBURN_WEAKNESS_TICKS, 0));
    }

    // Goat trait: same sprint-collision shape as Raging Bull, plus a self-stagger drawback on
    // a successful hit when the gene's downside effect is also present.
    private void tryRammingCharge(Player player) {
        Optional<PlayerGeneEffects.BehaviorMatch> match = PlayerGeneEffects.findBehavior(player, "ramming_charge");
        if (match.isEmpty()) {
            return;
        }

        Level level = player.level();
        long now = level.getGameTime();

        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(RAGING_BULL_COLLISION_MARGIN),
                e -> e instanceof LivingEntity && !(e instanceof Player) && e.isAlive())) {
            LivingEntity target = (LivingEntity) entity;
            Long cooldownEnd = rammingChargeHitCooldownEndTick.get(target.getUUID());
            if (cooldownEnd != null && now < cooldownEnd) {
                continue;
            }

            PlayerGeneEffects.BehaviorMatch behaviorMatch = match.get();
            float damage = (float) behaviorMatch.scaledValue();
            boolean hurt = target.hurt(level.damageSources().playerAttack(player), damage);
            if (hurt) {
                target.knockback(damage * RAMMING_CHARGE_KNOCKBACK_RATIO,
                        Mth.sin(player.getYRot() * Mth.DEG_TO_RAD),
                        -Mth.cos(player.getYRot() * Mth.DEG_TO_RAD));
                rammingChargeHitCooldownEndTick.put(target.getUUID(), now + behaviorMatch.effect().cooldownTicks());

                if (PlayerGeneEffects.hasBehavior(player, "ramming_charge_stagger")) {
                    player.knockback(RAMMING_CHARGE_STAGGER_KNOCKBACK,
                            -Mth.sin(player.getYRot() * Mth.DEG_TO_RAD),
                            Mth.cos(player.getYRot() * Mth.DEG_TO_RAD));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, RAMMING_CHARGE_STAGGER_SLOWNESS_TICKS, 0));
                }
            }
        }
    }

    // Percent-style behavior values are stored pre-multiplied by 100 (see BehaviorGeneEffect) so
    // tooltips print "20" and the JSON description supplies the "%" - divide back down here.
    private static float scaleUp(float amount, PlayerGeneEffects.BehaviorMatch match) {
        return amount * (1.0F + (float) (match.scaledValue() / 100.0));
    }

    // Skeleton's bow bonus, spider's magic vulnerability, and wolf's pack bonus all scale
    // damage rather than apply/remove anything - handled pre-finalization so the scaled amount
    // still goes through armor/enchantment resolution normally.
    @SubscribeEvent
    public void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntity();

        if (source.is(DamageTypes.ARROW)) {
            if (source.getEntity() instanceof Player shooter) {
                PlayerGeneEffects.findBehavior(shooter, "steady_aim_bow")
                        .ifPresent(m -> event.setAmount(scaleUp(event.getAmount(), m)));
            }
            return;
        }

        if (source.is(DamageTypes.MAGIC) && victim instanceof Player victimPlayer) {
            PlayerGeneEffects.findBehavior(victimPlayer, "arachnid_climb_frailty")
                    .ifPresent(m -> event.setAmount(scaleUp(event.getAmount(), m)));
        }

        if (source.getEntity() instanceof Player attacker) {
            PlayerGeneEffects.findBehavior(attacker, "pack_instinct").ifPresent(m -> {
                boolean nearWolf = !attacker.level().getEntitiesOfClass(Wolf.class,
                        attacker.getBoundingBox().inflate(PACK_INSTINCT_RADIUS), Wolf::isTame).isEmpty();
                if (nearWolf) {
                    event.setAmount(scaleUp(event.getAmount(), m));
                }
            });
        }
    }

    // Zombie's lifesteal and cave spider's poison both need the real dealt damage (post-armor),
    // so these hook after finalization rather than alongside the scaling handler above.
    @SubscribeEvent
    public void onLivingDamagePost(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (source.is(DamageTypes.ARROW) || !(source.getEntity() instanceof Player attacker)) {
            return;
        }

        float damageDealt = event.getNewDamage();
        if (damageDealt <= 0.0F) {
            return;
        }

        PlayerGeneEffects.findBehavior(attacker, "undying_hunger_lifesteal")
                .ifPresent(m -> attacker.heal(damageDealt * (float) (m.scaledValue() / 100.0)));

        PlayerGeneEffects.findBehavior(attacker, "venom_glands").ifPresent(m -> {
            int durationTicks = (int) Math.round(m.scaledValue() * 20.0);
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, durationTicks, 0));
        });
    }

    // Shared by Ruminant Gut (multiplier < 1, dampens) and Equine Gait's hunger drawback
    // (multiplier > 1, amplifies) - each trait gets its own cache map so having both
    // simultaneously doesn't cross-contaminate the other's delta tracking.
    private static void scaleExhaustionGain(Player player, Map<UUID, Float> cache, float multiplier) {
        FoodData foodData = player.getFoodData();
        float current = foodData.getExhaustionLevel();
        Float previous = cache.get(player.getUUID());
        if (previous != null) {
            float delta = current - previous;
            if (delta > 0) {
                foodData.setExhaustion(previous + delta * multiplier);
                current = foodData.getExhaustionLevel();
            }
        }
        cache.put(player.getUUID(), current);
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        if (PlayerGeneEffects.hasBehavior(player, "silent_step")) {
            event.setDamageMultiplier(0.0F);
            return;
        }
        if (PlayerGeneEffects.hasBehavior(player, "hollow_bones")) {
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
