package com.chimera;

import com.chimera.machine.GeneSequencerBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ChimeraMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneSequencerBlockEntity>> GENE_SEQUENCER =
            BLOCK_ENTITY_TYPES.register("gene_sequencer", () -> BlockEntityType.Builder.of(
                    GeneSequencerBlockEntity::new, ChimeraBlocks.GENE_SEQUENCER.get()).build(null));
}
