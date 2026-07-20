package com.chimera;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraPotions {

    private static final int STRESS_DURATION_TICKS = 3600;

    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, ChimeraMod.MODID);

    // Hunt gate (v0.2 tier-2 work order Milestone 3): brewed from Awkward Potion + Adrenal
    // Extract (see the RegisterBrewingRecipesEvent listener in ChimeraMod). Splash/Lingering
    // variants need no extra registration - vanilla's PotionBrewing.bootstrap already registers
    // generic container-upgrade recipes that apply to every potion type automatically.
    public static final DeferredHolder<Potion, Potion> STRESS = POTIONS.register("stress",
            () -> new Potion(new MobEffectInstance(ChimeraMobEffects.STRESSED, STRESS_DURATION_TICKS)));
}
