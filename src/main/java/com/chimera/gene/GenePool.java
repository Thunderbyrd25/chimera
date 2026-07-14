package com.chimera.gene;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public record GenePool(int tier, List<GenePool.WeightedGene> genes) {

    public static final Codec<GenePool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("tier").forGetter(GenePool::tier),
            WeightedGene.CODEC.listOf().fieldOf("genes").forGetter(GenePool::genes)
    ).apply(instance, GenePool::new));

    public record WeightedGene(ResourceLocation gene, int weight) {
        public static final Codec<WeightedGene> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("gene").forGetter(WeightedGene::gene),
                Codec.INT.fieldOf("weight").forGetter(WeightedGene::weight)
        ).apply(instance, WeightedGene::new));
    }
}
