package com.chimera;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ChimeraMod.MODID);

    // Hunt gate (v0.2 tier-2 work order Milestone 3): purely a detectable marker for the Potion
    // of Stress - no attribute modifiers, no tick behavior. TissueScraperItem checks for this via
    // LivingEntity#hasEffect() to decide whether a scrape yields Stress Plasma.
    public static final DeferredHolder<MobEffect, MobEffect> STRESSED = MOB_EFFECTS.register("stressed",
            () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xCC4433) {});
}
