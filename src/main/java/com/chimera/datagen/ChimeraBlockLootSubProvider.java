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
        dropSelf(ChimeraBlocks.GENOME_ANALYZER.get());
        dropSelf(ChimeraBlocks.GENE_EXTRACTOR.get());
        dropSelf(ChimeraBlocks.CENTRIFUGE.get());
        dropSelf(ChimeraBlocks.GENOME_SPLICER.get());
        dropSelf(ChimeraBlocks.BIOREACTOR.get());
        dropSelf(ChimeraBlocks.SYNTHESIZER.get());
        dropSelf(ChimeraBlocks.GESTATION_VAT.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(ChimeraBlocks.GENE_SEQUENCER.get(), ChimeraBlocks.GENOME_ANALYZER.get(), ChimeraBlocks.GENE_EXTRACTOR.get(),
                ChimeraBlocks.CENTRIFUGE.get(), ChimeraBlocks.GENOME_SPLICER.get(), ChimeraBlocks.BIOREACTOR.get(), ChimeraBlocks.SYNTHESIZER.get(),
                ChimeraBlocks.GESTATION_VAT.get());
    }
}
