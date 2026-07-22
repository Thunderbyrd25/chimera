package com.chimera.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// Byproduct economy work order Milestone 2b: reworked from an initial wild-wolf-taming idea
// (called too niche) into a bonemeal-style tool - per user clarification, "bonemeal-style" means
// the actual vanilla crop/sapling growth effect (reused directly via BoneMealItem.growCrop, not
// reimplemented), not just an animal-growth analogy. Works on both blocks (real bonemeal
// behavior) and animals (this item's own extension beyond real bonemeal). Reusable per the
// user's explicit correction (a durability tool, like TissueScraperItem, not a stack of
// throwaway consumables) - hurtAndBreak instead of shrink/vanilla bonemeal's per-use count.
public class AdrenalineDraughtItem extends Item {

    public AdrenalineDraughtItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // growCrop is marked @Deprecated by Mojang (as many vanilla helpers are) but is still
        // the real, only way to trigger a block's actual bonemeal behavior without manually
        // reimplementing BonemealableBlock's isValidBonemealTarget/isBonemealSuccess/
        // performBonemeal orchestration - intentional, not an oversight.
        ItemStack stack = context.getItemInHand();
        if (!BoneMealItem.growCrop(stack, level, context.getClickedPos())) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player != null) {
            EquipmentSlot slot = context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, slot);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        boolean growsBaby = interactionTarget instanceof AgeableMob ageable && ageable.isBaby();
        boolean readiesBreeding = interactionTarget instanceof Animal animal && animal.canFallInLove() && !animal.isInLove();
        if (!growsBaby && !readiesBreeding) {
            return InteractionResult.PASS;
        }

        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (growsBaby) {
            ((AgeableMob) interactionTarget).setAge(0);
        } else {
            ((Animal) interactionTarget).setInLove(player);
        }

        EquipmentSlot slot = usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);

        return InteractionResult.CONSUME;
    }
}
