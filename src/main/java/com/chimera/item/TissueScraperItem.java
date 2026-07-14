package com.chimera.item;

import java.util.Set;

import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;

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

    // v0.1 hardcodes the scrapable species here. Phase 3 introduces datapack-driven gene pools
    // (see CLAUDE.md architecture rule #1); once that registry exists, this should be replaced by
    // "does a gene pool exist for this entity type" so adding a mob never touches this class again.
    private static final Set<EntityType<?>> SCRAPABLE_MOBS = Set.of(EntityType.COW, EntityType.PIG, EntityType.CHICKEN, EntityType.SHEEP);

    public TissueScraperItem(Properties properties) {
        super(properties);
    }

    // Overridden by the reinforced tier for a chance at a bonus sample per scrape.
    protected float bonusSampleChance() {
        return 0.0F;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!SCRAPABLE_MOBS.contains(interactionTarget.getType())) {
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
