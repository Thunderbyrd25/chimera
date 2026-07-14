package com.chimera;

import com.chimera.machine.GeneSequencerBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChimeraMod.MODID);

    public static final DeferredBlock<Block> GENE_SEQUENCER = BLOCKS.register("gene_sequencer",
            () -> new GeneSequencerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()));
}
