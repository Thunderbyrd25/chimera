package com.chimera.item;

import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.gene.GenePool;
import com.chimera.gene.GenePoolRegistry;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TissueScraperItem extends Item {

    public TissueScraperItem(Properties properties) {
        super(properties);
    }

    // Overridden by the reinforced tier for a chance at a bonus sample per scrape.
    protected float bonusSampleChance() {
        return 0.0F;
    }

    // Overridden by the Apex tier to unlock tier-2 mobs (horse/zombie/skeleton/spider). A mob is
    // scrapable purely by having a gene pool at or below this tier - no hardcoded mob list, so
    // adding a new mob (at any tier) never touches this class (CLAUDE.md architecture rule #1).
    protected int maxTier() {
        return 1;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        GenePool pool = GenePoolRegistry.get(interactionTarget.getType());
        if (pool == null || pool.tier() > maxTier()) {
            return InteractionResult.PASS;
        }

        Level level = player.level();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        giveSample(player, interactionTarget);
        if (level.random.nextFloat() < bonusSampleChance()) {
            giveSample(player, interactionTarget);
        }

        EquipmentSlot slot = usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);

        return InteractionResult.CONSUME;
    }

    private void giveSample(Player player, LivingEntity target) {
        ItemStack sample = new ItemStack(ChimeraItems.TISSUE_SAMPLE.get());
        sample.set(ChimeraDataComponents.SPECIES.get(), EntityType.getKey(target.getType()));
        if (!player.getInventory().add(sample)) {
            player.drop(sample, false);
        }
    }
}
