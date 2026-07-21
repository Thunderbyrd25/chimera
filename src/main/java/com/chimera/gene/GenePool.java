package com.chimera.gene;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

// specificByproduct: byproduct-economy work order Milestone 1 - the mob-unique item scraping
// and sequencing can yield alongside the generic byproduct roll. Optional since not every pool
// has a kit yet (e.g. enderman.json, tier 3 placeholder with no byproduct assigned).
public record GenePool(int tier, List<GenePool.Entry> genes, Optional<ResourceLocation> specificByproduct) {

    public static final Codec<GenePool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("tier").forGetter(GenePool::tier),
            Entry.CODEC.listOf().fieldOf("genes").forGetter(GenePool::genes),
            ResourceLocation.CODEC.optionalFieldOf("specific_byproduct").forGetter(GenePool::specificByproduct)
    ).apply(instance, GenePool::new));

    // Every entry rolls independently (its own inclusion chance), not one exclusive pick among
    // the pool - a genome can carry 0..N of a pool's traits. If every roll fails, one entry is
    // forced in anyway so a full Sequencer+Analyzer cycle never comes back empty.
    public List<GeneInstance> rollGenes(RandomSource random) {
        List<GeneInstance> rolled = new ArrayList<>();
        for (Entry entry : genes) {
            if (random.nextDouble() < entry.chance()) {
                rolled.add(new GeneInstance(entry.gene(), rollStarLevel(entry.starWeights(), random)));
            }
        }
        if (rolled.isEmpty() && !genes.isEmpty()) {
            Entry fallback = genes.get(random.nextInt(genes.size()));
            rolled.add(new GeneInstance(fallback.gene(), rollStarLevel(fallback.starWeights(), random)));
        }
        return rolled;
    }

    private static int rollStarLevel(List<Integer> starWeights, RandomSource random) {
        int totalWeight = starWeights.stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) {
            return 1;
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < starWeights.size(); i++) {
            cumulative += starWeights.get(i);
            if (roll < cumulative) {
                return i + 1;
            }
        }
        return starWeights.size();
    }

    // chance: independent probability (0.0-1.0) this gene is included at all.
    // starWeights: relative weight per star level (index 0 = 1 star, ..., matching Gene.MAX_STAR_LEVEL entries).
    public record Entry(ResourceLocation gene, double chance, List<Integer> starWeights) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("gene").forGetter(Entry::gene),
                Codec.DOUBLE.fieldOf("chance").forGetter(Entry::chance),
                Codec.INT.listOf().fieldOf("star_weights").forGetter(Entry::starWeights)
        ).apply(instance, Entry::new));
    }
}
