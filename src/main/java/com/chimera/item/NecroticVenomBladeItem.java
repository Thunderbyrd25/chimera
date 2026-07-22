package com.chimera.item;

import com.chimera.ChimeraAttachments;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

// Byproduct economy work order Milestone 3a: crafted from Necrotic Ichor + Venom Sac. Poisons on
// hit; the wielder's lifesteal from each resulting poison tick is handled separately in
// combat/NecroticVenomHandler, which reads the POISON_BLADE_ATTACKER attachment this class sets -
// poison's own damage carries no attacker reference at all, so tracking "who poisoned this mob
// with this weapon" has to happen here.
public class NecroticVenomBladeItem extends SwordItem {

    private static final int POISON_DURATION_TICKS = 80;
    private static final int POISON_AMPLIFIER = 0;

    public NecroticVenomBladeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!target.level().isClientSide && attacker instanceof Player player) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, POISON_AMPLIFIER));
            target.setData(ChimeraAttachments.POISON_BLADE_ATTACKER.get(), player.getUUID());
        }
        return result;
    }
}
