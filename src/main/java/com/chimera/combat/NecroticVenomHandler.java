package com.chimera.combat;

import java.util.UUID;

import com.chimera.ChimeraAttachments;

import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

// Byproduct economy work order Milestone 3a: the Necrotic Venom Blade (item/NecroticVenomBladeItem)
// sets ChimeraAttachments.POISON_BLADE_ATTACKER on a target it poisons. Poison's own periodic
// damage source carries no attacker reference at all (confirmed - NeoForge's poison DamageSource
// is built with no entity), so the lifesteal has to be driven from here, reading that attachment
// back, rather than from the damage event's own (always-null) source entity.
public class NecroticVenomHandler {

    private static final float HEAL_PER_POISON_TICK = 1.0F;

    @SubscribeEvent
    public void onLivingDamagePost(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!source.is(NeoForgeMod.POISON_DAMAGE)) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        UUID attackerId = target.getData(ChimeraAttachments.POISON_BLADE_ATTACKER.get());
        if (attackerId.equals(Util.NIL_UUID)) {
            return;
        }

        ServerPlayer attacker = serverLevel.getServer().getPlayerList().getPlayer(attackerId);
        if (attacker != null) {
            attacker.heal(HEAL_PER_POISON_TICK);
        }
    }
}
