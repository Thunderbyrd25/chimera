package com.chimera;

import com.chimera.machine.BioreactorBlock;
import com.chimera.machine.CentrifugeBlock;
import com.chimera.machine.GeneExtractorBlock;
import com.chimera.machine.GeneSequencerBlock;
import com.chimera.machine.GenomeAnalyzerBlock;
import com.chimera.machine.GenomeSplicerBlock;
import com.chimera.machine.SynthesizerBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChimeraMod.MODID);

    public static final DeferredBlock<Block> GENE_SEQUENCER = BLOCKS.register("gene_sequencer",
            () -> new GeneSequencerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GENOME_ANALYZER = BLOCKS.register("genome_analyzer",
            () -> new GenomeAnalyzerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GENE_EXTRACTOR = BLOCKS.register("gene_extractor",
            () -> new GeneExtractorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> CENTRIFUGE = BLOCKS.register("centrifuge",
            () -> new CentrifugeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GENOME_SPLICER = BLOCKS.register("genome_splicer",
            () -> new GenomeSplicerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BIOREACTOR = BLOCKS.register("bioreactor",
            () -> new BioreactorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> SYNTHESIZER = BLOCKS.register("synthesizer",
            () -> new SynthesizerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()));
}
