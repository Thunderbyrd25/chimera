package com.chimera.gene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Marker for effects that can't be expressed as an attribute modifier (tick loops, event
// hooks, active abilities - e.g. Ruminant Gut's hunger drain, Hollow Bones' fall damage
// reduction, Grass Fed's hunger-on-demand, Raging Bull's charge damage). The behaviorId is
// matched against hardcoded Java logic in GeneEffectHandlers/PlayerGeneEffects; this record
// only carries the data, it does not implement the behavior itself.
// baseValue/perLevelValue/cooldownTicks are generic linear-scaling knobs (amount = baseValue +
// perLevelValue * (starLevel - 1)) for behaviors that need them - all default to 0, so
// Ruminant Gut/Hollow Bones/freeze_immunity (still hardcoded, unscaled) don't need JSON changes.
public record BehaviorGeneEffect(String behaviorId, double baseValue, double perLevelValue, int cooldownTicks) implements GeneEffect {

    public static final String TYPE = "behavior";

    public static final MapCodec<BehaviorGeneEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("behavior_id").forGetter(BehaviorGeneEffect::behaviorId),
            Codec.DOUBLE.optionalFieldOf("base_value", 0.0).forGetter(BehaviorGeneEffect::baseValue),
            Codec.DOUBLE.optionalFieldOf("per_level_value", 0.0).forGetter(BehaviorGeneEffect::perLevelValue),
            Codec.INT.optionalFieldOf("cooldown_ticks", 0).forGetter(BehaviorGeneEffect::cooldownTicks)
    ).apply(instance, BehaviorGeneEffect::new));

    public double scaledValue(int starLevel) {
        return baseValue + perLevelValue * (starLevel - 1);
    }

    @Override
    public String type() {
        return TYPE;
    }
}
