package com.chimera.gene;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface GeneEffect {

    Map<String, MapCodec<? extends GeneEffect>> TYPES = Map.of(
            AttributeModifierGeneEffect.TYPE, AttributeModifierGeneEffect.CODEC,
            BehaviorGeneEffect.TYPE, BehaviorGeneEffect.CODEC
    );

    Codec<GeneEffect> CODEC = Codec.STRING.dispatch("type", GeneEffect::type, id -> {
        MapCodec<? extends GeneEffect> codec = TYPES.get(id);
        if (codec == null) {
            throw new IllegalArgumentException("Unknown gene effect type: " + id);
        }
        return codec;
    });

    String type();
}
