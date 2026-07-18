package com.chimera.gene;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.chat.Component;

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

    // True for a gene's downside half. Purely a display concern - application/removal doesn't
    // branch on this at all, since both attribute and behavior effects already derive fresh
    // from equipped-item data every time rather than being applied/removed as discrete steps
    // (see ChimeraCuriosCompat/PlayerGeneEffects), so a negative-amount effect just works.
    boolean drawback();

    // Human-readable magnitude at the given star level, for tooltips. Empty component if this
    // effect has nothing describable yet (e.g. a behavior with no "description" authored).
    Component describe(int starLevel);
}
