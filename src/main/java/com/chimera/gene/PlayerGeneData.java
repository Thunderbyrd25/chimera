package com.chimera.gene;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Canonical record of which genes are currently active for a player, kept independent of
// Curios so behavior-effect logic (tick/event hooks) never needs to know Curios exists -
// only the equip/unequip integration in the curios package writes to this. Carries star level
// (not just gene id) so level-scaled behavior effects (Milestone 2) can read it directly.
// corruption is a future hook (CLAUDE.md architecture rule #6): unused in v0.1, always 0.
public record PlayerGeneData(List<GeneInstance> installedGenes, float corruption) {

    public static final PlayerGeneData EMPTY = new PlayerGeneData(List.of(), 0.0F);

    public static final Codec<PlayerGeneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GeneInstance.CODEC.listOf().fieldOf("installed_genes").forGetter(PlayerGeneData::installedGenes),
            Codec.FLOAT.fieldOf("corruption").forGetter(PlayerGeneData::corruption)
    ).apply(instance, PlayerGeneData::new));

    public PlayerGeneData withGeneAdded(GeneInstance gene) {
        if (installedGenes.contains(gene)) {
            return this;
        }
        List<GeneInstance> updated = new ArrayList<>(installedGenes);
        updated.add(gene);
        return new PlayerGeneData(List.copyOf(updated), corruption);
    }

    public PlayerGeneData withGeneRemoved(GeneInstance gene) {
        if (!installedGenes.contains(gene)) {
            return this;
        }
        List<GeneInstance> updated = new ArrayList<>(installedGenes);
        updated.remove(gene);
        return new PlayerGeneData(List.copyOf(updated), corruption);
    }
}
