package com.chimera.gene;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

// Canonical record of which genes are currently active for a player, kept independent of
// Curios so behavior-effect logic (tick/event hooks) never needs to know Curios exists -
// only the equip/unequip integration in the curios package writes to this.
// corruption is a future hook (CLAUDE.md architecture rule #6): unused in v0.1, always 0.
public record PlayerGeneData(List<ResourceLocation> installedGenes, float corruption) {

    public static final PlayerGeneData EMPTY = new PlayerGeneData(List.of(), 0.0F);

    public static final Codec<PlayerGeneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("installed_genes").forGetter(PlayerGeneData::installedGenes),
            Codec.FLOAT.fieldOf("corruption").forGetter(PlayerGeneData::corruption)
    ).apply(instance, PlayerGeneData::new));

    public PlayerGeneData withGeneAdded(ResourceLocation gene) {
        if (installedGenes.contains(gene)) {
            return this;
        }
        List<ResourceLocation> updated = new java.util.ArrayList<>(installedGenes);
        updated.add(gene);
        return new PlayerGeneData(List.copyOf(updated), corruption);
    }

    public PlayerGeneData withGeneRemoved(ResourceLocation gene) {
        if (!installedGenes.contains(gene)) {
            return this;
        }
        List<ResourceLocation> updated = new java.util.ArrayList<>(installedGenes);
        updated.remove(gene);
        return new PlayerGeneData(List.copyOf(updated), corruption);
    }
}
