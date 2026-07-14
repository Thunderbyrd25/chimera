package com.chimera.gene;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public record GenePool(int tier, List<GenePool.WeightedGene> genes) {

    public static final Codec<GenePool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("tier").forGetter(GenePool::tier),
            WeightedGene.CODEC.listOf().fieldOf("genes").forGetter(GenePool::genes)
    ).apply(instance, GenePool::new));

    // Weighted random pick. Every v0.1 pool has exactly one entry at weight 100, but this
    // stays genuinely weighted so adding a second gene to a pool (pure data change) works
    // without touching this code.
    @Nullable
    public ResourceLocation rollGene(RandomSource random) {
        int totalWeight = genes.stream().mapToInt(WeightedGene::weight).sum();
        if (totalWeight <= 0) {
            return null;
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (WeightedGene weighted : genes) {
            cumulative += weighted.weight();
            if (roll < cumulative) {
                return weighted.gene();
            }
        }
        return genes.get(genes.size() - 1).gene();
    }

    public record WeightedGene(ResourceLocation gene, int weight) {
        public static final Codec<WeightedGene> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("gene").forGetter(WeightedGene::gene),
                Codec.INT.fieldOf("weight").forGetter(WeightedGene::weight)
        ).apply(instance, WeightedGene::new));
    }
}
