package com.chimera.gene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Marker for effects that can't be expressed as an attribute modifier (tick loops, event
// hooks - e.g. Ruminant Gut's hunger drain, Hollow Bones' fall damage reduction). The
// behaviorId is matched against hardcoded Java logic wired up in Phase 6; this record only
// carries the data, it does not implement the behavior itself.
public record BehaviorGeneEffect(String behaviorId) implements GeneEffect {

    public static final String TYPE = "behavior";

    public static final MapCodec<BehaviorGeneEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("behavior_id").forGetter(BehaviorGeneEffect::behaviorId)
    ).apply(instance, BehaviorGeneEffect::new));

    @Override
    public String type() {
        return TYPE;
    }
}
