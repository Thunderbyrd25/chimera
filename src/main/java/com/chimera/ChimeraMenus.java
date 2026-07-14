package com.chimera;

import com.chimera.machine.GeneExtractorBlockEntity;
import com.chimera.machine.GeneExtractorMenu;
import com.chimera.machine.GeneSequencerBlockEntity;
import com.chimera.machine.GeneSequencerMenu;
import com.chimera.machine.GenomeAnalyzerBlockEntity;
import com.chimera.machine.GenomeAnalyzerMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ChimeraMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GeneSequencerMenu>> GENE_SEQUENCER =
            MENU_TYPES.register("gene_sequencer", () -> new MenuType<>((IContainerFactory<GeneSequencerMenu>) (containerId, inventory, buf) -> {
                GeneSequencerBlockEntity blockEntity = (GeneSequencerBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new GeneSequencerMenu(containerId, inventory, blockEntity);
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<GenomeAnalyzerMenu>> GENOME_ANALYZER =
            MENU_TYPES.register("genome_analyzer", () -> new MenuType<>((IContainerFactory<GenomeAnalyzerMenu>) (containerId, inventory, buf) -> {
                GenomeAnalyzerBlockEntity blockEntity = (GenomeAnalyzerBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new GenomeAnalyzerMenu(containerId, inventory, blockEntity);
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<GeneExtractorMenu>> GENE_EXTRACTOR =
            MENU_TYPES.register("gene_extractor", () -> new MenuType<>((IContainerFactory<GeneExtractorMenu>) (containerId, inventory, buf) -> {
                GeneExtractorBlockEntity blockEntity = (GeneExtractorBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new GeneExtractorMenu(containerId, inventory, blockEntity);
            }, FeatureFlags.VANILLA_SET));
}
