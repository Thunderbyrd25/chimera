package com.chimera;

import org.slf4j.Logger;

import com.chimera.datagen.ChimeraDataGenerators;
import com.chimera.gene.GenePoolRegistry;
import com.chimera.gene.GeneRegistry;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here must match the modId entry in META-INF/neoforge.mods.toml
@Mod(ChimeraMod.MODID)
public class ChimeraMod {

    public static final String MODID = "chimera";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Every registrable type goes through a DeferredRegister. See CLAUDE.md architecture rule #3.
    // Each kind of registrable owns its DeferredRegister in its own class (ChimeraItems,
    // ChimeraBlocks, ChimeraBlockEntities, ChimeraMenus, ChimeraDataComponents); this class
    // just wires them all up, plus the creative tab that ties everything together.
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CHIMERA_TAB = CREATIVE_MODE_TABS.register(
            "chimera_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.chimera"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ChimeraItems.TISSUE_SCRAPER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ChimeraItems.TISSUE_SCRAPER.get());
                        output.accept(ChimeraItems.TISSUE_SAMPLE.get());
                        output.accept(ChimeraItems.SEQUENCED_GENOME.get());
                        output.accept(ChimeraItems.NUTRIENT_AGAR.get());
                        output.accept(ChimeraItems.CELL_CULTURE.get());
                        output.accept(ChimeraItems.NUCLEOTIDE_SLURRY.get());
                        output.accept(ChimeraItems.CHROMATIN_STRAND.get());
                        output.accept(ChimeraItems.MUTAGEN.get());
                        output.accept(ChimeraItems.BLANK_GENE_CASSETTE.get());
                        output.accept(ChimeraItems.GENE_CASSETTE.get());
                        output.accept(ChimeraItems.GENE_SEQUENCER.get());
                        output.accept(ChimeraItems.GENOME_ANALYZER.get());
                        output.accept(ChimeraItems.GENE_EXTRACTOR.get());
                    }).build());

    public ChimeraMod(IEventBus modEventBus, ModContainer modContainer) {
        ChimeraBlocks.BLOCKS.register(modEventBus);
        ChimeraBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ChimeraMenus.MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ChimeraItems.ITEMS.register(modEventBus);
        ChimeraDataComponents.DATA_COMPONENTS.register(modEventBus);

        modEventBus.addListener(ChimeraDataGenerators::gatherData);

        modEventBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.GENE_SEQUENCER.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.GENOME_ANALYZER.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.GENE_EXTRACTOR.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
        });

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new GeneRegistry());
            event.addListener(new GenePoolRegistry());
        });
    }
}
