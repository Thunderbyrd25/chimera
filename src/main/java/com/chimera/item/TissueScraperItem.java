package com.chimera.item;

import com.chimera.ChimeraAttachments;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.ChimeraMobEffects;
import com.chimera.gene.ByproductRoller;
import com.chimera.gene.GenePool;
import com.chimera.gene.GenePoolRegistry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TissueScraperItem extends Item {

    // Hunt gate (v0.2 tier-2 work order Milestone 3): a mob scraped within the last day yields
    // nothing at all, regardless of scraper tier - pacing, not a tier gate.
    private static final long SCRAPE_COOLDOWN_TICKS = 24000L;

    public TissueScraperItem(Properties properties) {
        super(properties);
    }

    // Overridden by higher tiers for a chance at a bonus sample/Stress Plasma per scrape.
    protected float bonusSampleChance() {
        return 0.0F;
    }

    // Overridden by the Apex tier to unlock tier-2 mobs (horse/zombie/skeleton/spider). A mob is
    // scrapable purely by having a gene pool at or below this tier - no hardcoded mob list, so
    // adding a new mob (at any tier) never touches this class (CLAUDE.md architecture rule #1).
    protected int maxTier() {
        return 1;
    }

    // Biopedia+Oath work order Milestone 2: right-clicking air (no entity target) samples the
    // player's own DNA. Only fires here since interactLivingEntity above already claims every
    // entity-target right-click - this is purely the empty-air fallback.
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ItemStack sample = new ItemStack(ChimeraItems.SELF_TISSUE_SAMPLE.get());
        sample.set(ChimeraDataComponents.SPECIES.get(), TissueSampleItem.PLAYER_SPECIES);
        player.drop(sample, false);

        EquipmentSlot slot = usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        Level level = player.level();
        long now = level.getGameTime();
        long lastScraped = interactionTarget.getData(ChimeraAttachments.LAST_SCRAPED_TIME.get());
        if (lastScraped != 0L && now - lastScraped < SCRAPE_COOLDOWN_TICKS) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.chimera.scrape_cooldown"), true);
            }
            return InteractionResult.PASS;
        }

        GenePool pool = GenePoolRegistry.get(interactionTarget.getType());
        boolean tierEligible = pool != null && pool.tier() <= maxTier();
        // Stress Plasma requires a deliberate Potion of Stress splash (see ChimeraMobEffects,
        // ChimeraPotions) rather than ambient combat state - any scraper tier that successfully
        // reads this mob can harvest it, since the gate is the potion setup, not tool tier.
        boolean stressed = interactionTarget.hasEffect(ChimeraMobEffects.STRESSED);

        if (!tierEligible && !stressed) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (tierEligible) {
            giveSample(player, interactionTarget);
            if (level.random.nextFloat() < bonusSampleChance()) {
                giveSample(player, interactionTarget);
            }
            giveByproducts(player, pool);
        }
        if (stressed) {
            giveStressPlasma(player);
            if (level.random.nextFloat() < bonusSampleChance()) {
                giveStressPlasma(player);
            }
        }

        interactionTarget.setData(ChimeraAttachments.LAST_SCRAPED_TIME.get(), now);

        EquipmentSlot slot = usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);

        return InteractionResult.CONSUME;
    }

    // Scraped materials drop on the ground rather than going straight to the player's
    // inventory (explicit design call - makes the "you had to go pick this up" beat of
    // harvesting visible instead of silent).
    private void giveSample(Player player, LivingEntity target) {
        ItemStack sample = new ItemStack(ChimeraItems.TISSUE_SAMPLE.get());
        sample.set(ChimeraDataComponents.SPECIES.get(), EntityType.getKey(target.getType()));
        player.drop(sample, false);
    }

    private void giveStressPlasma(Player player) {
        ItemStack plasma = new ItemStack(ChimeraItems.STRESS_PLASMA.get());
        player.drop(plasma, false);
    }

    // Byproduct economy work order Milestone 1: opportunistic source, unlike sequencing's rolls.
    // Scales off bonusSampleChance() (so the base scraper, 0% bonus chance, still never yields
    // byproducts from scraping alone, only Reinforced/Apex/Predator do) but dialed well below it
    // via BYPRODUCT_CHANCE_MULTIPLIER - a flat reuse of bonusSampleChance() felt too generous at
    // Predator tier per user feedback after the first hands-on pass. Decoupled from
    // bonusSampleChance() itself (rather than lowering that) so the bonus-Tissue-Sample rate is
    // untouched - this only tunes byproducts.
    private static final float BYPRODUCT_CHANCE_MULTIPLIER = 0.4F;

    private float byproductChance() {
        return bonusSampleChance() * BYPRODUCT_CHANCE_MULTIPLIER;
    }

    private void giveByproducts(Player player, GenePool pool) {
        Level level = player.level();
        if (level.random.nextFloat() < byproductChance()) {
            player.drop(ByproductRoller.rollGeneric(level.random, 0), false);
        }
        if (level.random.nextFloat() < byproductChance() && pool.specificByproduct().isPresent()) {
            ResourceLocation byproductId = pool.specificByproduct().get();
            player.drop(new ItemStack(BuiltInRegistries.ITEM.get(byproductId)), false);
        }
    }
}
