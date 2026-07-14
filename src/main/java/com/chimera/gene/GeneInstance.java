package com.chimera.gene;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

// A trait as actually carried by an item: which gene, and how good the roll was (1-3 stars,
// see Gene.MAX_STAR_LEVEL). Replaces the bare gene ResourceLocation the traits component used
// to hold in v0.1, where every instance of a gene was mechanically identical.
public record GeneInstance(ResourceLocation gene, int starLevel) {

    public static final Codec<GeneInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("gene").forGetter(GeneInstance::gene),
            Codec.INT.fieldOf("star_level").forGetter(GeneInstance::starLevel)
    ).apply(instance, GeneInstance::new));

    public static final StreamCodec<ByteBuf, GeneInstance> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, GeneInstance::gene,
            ByteBufCodecs.VAR_INT, GeneInstance::starLevel,
            GeneInstance::new);

    // If the same gene is installed more than once (e.g. two Bovine Vigor cassettes in a
    // Mk2/Mk3 core), only the highest star level should actually take effect - installing a
    // second, weaker copy shouldn't be able to downgrade an already-installed stronger one.
    public static List<GeneInstance> highestPerGene(List<GeneInstance> instances) {
        Map<ResourceLocation, GeneInstance> best = new LinkedHashMap<>();
        for (GeneInstance instance : instances) {
            best.merge(instance.gene(), instance, (a, b) -> a.starLevel() >= b.starLevel() ? a : b);
        }
        return List.copyOf(best.values());
    }
}
