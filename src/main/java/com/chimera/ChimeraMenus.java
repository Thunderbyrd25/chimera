package com.chimera;

import com.chimera.machine.BioreactorBlockEntity;
import com.chimera.machine.BioreactorMenu;
import com.chimera.machine.CentrifugeBlockEntity;
import com.chimera.machine.CentrifugeMenu;
import com.chimera.machine.GeneExtractorBlockEntity;
import com.chimera.machine.GeneExtractorMenu;
import com.chimera.machine.GeneSequencerBlockEntity;
import com.chimera.machine.GeneSequencerMenu;
import com.chimera.machine.GenomeAnalyzerBlockEntity;
import com.chimera.machine.GenomeAnalyzerMenu;
import com.chimera.machine.GenomeSplicerBlockEntity;
import com.chimera.machine.GenomeSplicerMenu;
import com.chimera.machine.GestationVatBlockEntity;
import com.chimera.machine.GestationVatMenu;
import com.chimera.machine.SynthesizerBlockEntity;
import com.chimera.machine.SynthesizerMenu;
import com.chimera.item.SpliceCoreItem;
import com.chimera.splice.SpliceCoreMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ChimeraMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GeneSequencerMenu>> GENE_SEQUENCER =
            MENU_TYPES.register("gene_sequencer", () -> new MenuType<>((IContainerFactory<GeneSequencerMenu>) (containerId, inventory, buf) -> {
                GeneSequencerBlockEntity blockEntity = (GeneSequencerBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new GeneSequencerMenu(containerId, inventory, blockEntity, buf.readVarInt());
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<GenomeAnalyzerMenu>> GENOME_ANALYZER =
            MENU_TYPES.register("genome_analyzer", () -> new MenuType<>((IContainerFactory<GenomeAnalyzerMenu>) (containerId, inventory, buf) -> {
                GenomeAnalyzerBlockEntity blockEntity = (GenomeAnalyzerBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new GenomeAnalyzerMenu(containerId, inventory, blockEntity, buf.readVarInt());
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<GeneExtractorMenu>> GENE_EXTRACTOR =
            MENU_TYPES.register("gene_extractor", () -> new MenuType<>((IContainerFactory<GeneExtractorMenu>) (containerId, inventory, buf) -> {
                GeneExtractorBlockEntity blockEntity = (GeneExtractorBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new GeneExtractorMenu(containerId, inventory, blockEntity, buf.readVarInt());
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CentrifugeMenu>> CENTRIFUGE =
            MENU_TYPES.register("centrifuge", () -> new MenuType<>((IContainerFactory<CentrifugeMenu>) (containerId, inventory, buf) -> {
                CentrifugeBlockEntity blockEntity = (CentrifugeBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new CentrifugeMenu(containerId, inventory, blockEntity, buf.readVarInt());
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<GenomeSplicerMenu>> GENOME_SPLICER =
            MENU_TYPES.register("genome_splicer", () -> new MenuType<>((IContainerFactory<GenomeSplicerMenu>) (containerId, inventory, buf) -> {
                GenomeSplicerBlockEntity blockEntity = (GenomeSplicerBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new GenomeSplicerMenu(containerId, inventory, blockEntity, buf.readVarInt());
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<BioreactorMenu>> BIOREACTOR =
            MENU_TYPES.register("bioreactor", () -> new MenuType<>((IContainerFactory<BioreactorMenu>) (containerId, inventory, buf) -> {
                BioreactorBlockEntity blockEntity = (BioreactorBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new BioreactorMenu(containerId, inventory, blockEntity, buf.readVarInt());
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<SynthesizerMenu>> SYNTHESIZER =
            MENU_TYPES.register("synthesizer", () -> new MenuType<>((IContainerFactory<SynthesizerMenu>) (containerId, inventory, buf) -> {
                SynthesizerBlockEntity blockEntity = (SynthesizerBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new SynthesizerMenu(containerId, inventory, blockEntity, buf.readVarInt());
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<GestationVatMenu>> GESTATION_VAT =
            MENU_TYPES.register("gestation_vat", () -> new MenuType<>((IContainerFactory<GestationVatMenu>) (containerId, inventory, buf) -> {
                GestationVatBlockEntity blockEntity = (GestationVatBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos());
                return new GestationVatMenu(containerId, inventory, blockEntity, buf.readVarInt());
            }, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<SpliceCoreMenu>> SPLICE_CORE =
            MENU_TYPES.register("splice_core", () -> new MenuType<>((IContainerFactory<SpliceCoreMenu>) (containerId, inventory, buf) -> {
                InteractionHand hand = buf.readEnum(InteractionHand.class);
                ItemStack coreStack = inventory.player.getItemInHand(hand);
                int slotCount = coreStack.getItem() instanceof SpliceCoreItem spliceCore ? spliceCore.getSlotCount() : 1;
                return new SpliceCoreMenu(containerId, inventory, hand, slotCount);
            }, FeatureFlags.VANILLA_SET));
}
