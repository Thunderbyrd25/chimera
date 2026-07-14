package com.chimera.gene;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// requiresAnima is a future hook (CLAUDE.md architecture rule #6): unused in v0.1, always false.
public record Gene(int tier, boolean requiresAnima, List<GeneEffect> effects) {

    public static final Codec<Gene> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("tier").forGetter(Gene::tier),
            Codec.BOOL.optionalFieldOf("requires_anima", false).forGetter(Gene::requiresAnima),
            GeneEffect.CODEC.listOf().fieldOf("effects").forGetter(Gene::effects)
    ).apply(instance, Gene::new));
}
