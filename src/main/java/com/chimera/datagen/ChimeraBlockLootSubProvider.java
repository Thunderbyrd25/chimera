package com.chimera.datagen;

import java.util.List;
import java.util.Set;

import com.chimera.ChimeraBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class ChimeraBlockLootSubProvider extends BlockLootSubProvider {

    protected ChimeraBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ChimeraBlocks.GENE_SEQUENCER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(ChimeraBlocks.GENE_SEQUENCER.get());
    }
}
