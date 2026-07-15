package com.chimera.datagen;

import com.chimera.ChimeraBlocks;
import com.chimera.ChimeraMod;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ChimeraBlockStateProvider extends BlockStateProvider {

    public ChimeraBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ChimeraMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ChimeraBlocks.GENE_SEQUENCER.get(), cubeAll(ChimeraBlocks.GENE_SEQUENCER.get()));
        simpleBlockWithItem(ChimeraBlocks.GENOME_ANALYZER.get(), cubeAll(ChimeraBlocks.GENOME_ANALYZER.get()));
        simpleBlockWithItem(ChimeraBlocks.GENE_EXTRACTOR.get(), cubeAll(ChimeraBlocks.GENE_EXTRACTOR.get()));
        simpleBlockWithItem(ChimeraBlocks.CENTRIFUGE.get(), cubeAll(ChimeraBlocks.CENTRIFUGE.get()));
        simpleBlockWithItem(ChimeraBlocks.GENOME_SPLICER.get(), cubeAll(ChimeraBlocks.GENOME_SPLICER.get()));
        simpleBlockWithItem(ChimeraBlocks.BIOREACTOR.get(), cubeAll(ChimeraBlocks.BIOREACTOR.get()));
    }
}
