package com.chimera.gene;

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
}
