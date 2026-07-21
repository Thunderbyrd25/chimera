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
// synthesisOutputs: byproduct-economy Milestone 2a - what the Synthesizer machine can roll when
// fed this mob's specific byproduct (a Scrap item, or a small already-granular vanilla item like
// Feather/String that skips the Scrap tier). Defaults to empty - only the 5 domesticated mobs
// have one so far; goat/fox/wolf are deferred to Milestone 2b (trinkets/tools instead), and
// hostiles/enderman never get one.
public record GenePool(int tier, List<GenePool.Entry> genes, Optional<ResourceLocation> specificByproduct,
        List<GenePool.SynthesisOutput> synthesisOutputs) {

    public static final Codec<GenePool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("tier").forGetter(GenePool::tier),
            Entry.CODEC.listOf().fieldOf("genes").forGetter(GenePool::genes),
            ResourceLocation.CODEC.optionalFieldOf("specific_byproduct").forGetter(GenePool::specificByproduct),
            SynthesisOutput.CODEC.listOf().optionalFieldOf("synthesis_outputs", List.of()).forGetter(GenePool::synthesisOutputs)
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

    // Cumulative-weight pick, same shape as rollStarLevel below - empty only if the pool has no
    // synthesis outputs at all (callers should check synthesisOutputs().isEmpty() first).
    public Optional<ResourceLocation> rollSynthesisOutput(RandomSource random) {
        if (synthesisOutputs.isEmpty()) {
            return Optional.empty();
        }
        int totalWeight = synthesisOutputs.stream().mapToInt(SynthesisOutput::weight).sum();
        int roll = random.nextInt(Math.max(1, totalWeight));
        int cumulative = 0;
        for (SynthesisOutput candidate : synthesisOutputs) {
            cumulative += candidate.weight();
            if (roll < cumulative) {
                return Optional.of(candidate.item());
            }
        }
        return Optional.of(synthesisOutputs.get(synthesisOutputs.size() - 1).item());
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

    public record SynthesisOutput(ResourceLocation item, int weight) {
        public static final Codec<SynthesisOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(SynthesisOutput::item),
                Codec.INT.fieldOf("weight").forGetter(SynthesisOutput::weight)
        ).apply(instance, SynthesisOutput::new));
    }
}
