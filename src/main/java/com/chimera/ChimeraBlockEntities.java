package com.chimera;

import com.chimera.machine.BioreactorBlockEntity;
import com.chimera.machine.CentrifugeBlockEntity;
import com.chimera.machine.GeneExtractorBlockEntity;
import com.chimera.machine.GeneSequencerBlockEntity;
import com.chimera.machine.GenomeAnalyzerBlockEntity;
import com.chimera.machine.GenomeSplicerBlockEntity;
import com.chimera.machine.GestationVatBlockEntity;
import com.chimera.machine.SynthesizerBlockEntity;

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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GenomeAnalyzerBlockEntity>> GENOME_ANALYZER =
            BLOCK_ENTITY_TYPES.register("genome_analyzer", () -> BlockEntityType.Builder.of(
                    GenomeAnalyzerBlockEntity::new, ChimeraBlocks.GENOME_ANALYZER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneExtractorBlockEntity>> GENE_EXTRACTOR =
            BLOCK_ENTITY_TYPES.register("gene_extractor", () -> BlockEntityType.Builder.of(
                    GeneExtractorBlockEntity::new, ChimeraBlocks.GENE_EXTRACTOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CentrifugeBlockEntity>> CENTRIFUGE =
            BLOCK_ENTITY_TYPES.register("centrifuge", () -> BlockEntityType.Builder.of(
                    CentrifugeBlockEntity::new, ChimeraBlocks.CENTRIFUGE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GenomeSplicerBlockEntity>> GENOME_SPLICER =
            BLOCK_ENTITY_TYPES.register("genome_splicer", () -> BlockEntityType.Builder.of(
                    GenomeSplicerBlockEntity::new, ChimeraBlocks.GENOME_SPLICER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BioreactorBlockEntity>> BIOREACTOR =
            BLOCK_ENTITY_TYPES.register("bioreactor", () -> BlockEntityType.Builder.of(
                    BioreactorBlockEntity::new, ChimeraBlocks.BIOREACTOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SynthesizerBlockEntity>> SYNTHESIZER =
            BLOCK_ENTITY_TYPES.register("synthesizer", () -> BlockEntityType.Builder.of(
                    SynthesizerBlockEntity::new, ChimeraBlocks.SYNTHESIZER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GestationVatBlockEntity>> GESTATION_VAT =
            BLOCK_ENTITY_TYPES.register("gestation_vat", () -> BlockEntityType.Builder.of(
                    GestationVatBlockEntity::new, ChimeraBlocks.GESTATION_VAT.get()).build(null));
}
