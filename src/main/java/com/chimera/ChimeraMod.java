package com.chimera;

import org.slf4j.Logger;

import com.chimera.datagen.ChimeraDataGenerators;
import com.chimera.gene.GeneEffectHandlers;
import com.chimera.gene.GenePoolRegistry;
import com.chimera.gene.GeneRegistry;
import com.chimera.item.TissueScraperEventHandler;
import com.chimera.network.ChimeraPayloads;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here must match the modId entry in META-INF/neoforge.mods.toml
@Mod(ChimeraMod.MODID)
public class ChimeraMod {

    public static final String MODID = "chimera";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Every registrable type goes through a DeferredRegister. See CLAUDE.md architecture rule #3.
    // Each kind of registrable owns its DeferredRegister in its own class (ChimeraItems,
    // ChimeraBlocks, ChimeraBlockEntities, ChimeraMenus, ChimeraDataComponents,
    // ChimeraAttachments); this class just wires them all up, plus the creative tab that ties
    // everything together.
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
                        output.accept(ChimeraItems.REINFORCED_TISSUE_SCRAPER.get());
                        output.accept(ChimeraItems.APEX_TISSUE_SCRAPER.get());
                        output.accept(ChimeraItems.PREDATOR_TISSUE_SCRAPER.get());
                        output.accept(ChimeraItems.TISSUE_SAMPLE.get());
                        output.accept(ChimeraItems.STRESS_PLASMA.get());
                        output.accept(ChimeraItems.COMBAT_STIMULANT.get());
                        output.accept(ChimeraItems.ADRENAL_EXTRACT.get());
                        output.accept(ChimeraItems.SEQUENCED_GENOME.get());
                        output.accept(ChimeraItems.BIOMASS.get());
                        output.accept(ChimeraItems.CELL_CULTURE.get());
                        output.accept(ChimeraItems.NUCLEOTIDE_SLURRY.get());
                        output.accept(ChimeraItems.CHROMATIN_STRAND.get());
                        output.accept(ChimeraItems.MUTAGEN.get());
                        output.accept(ChimeraItems.REFINED_CULTURE.get());
                        output.accept(ChimeraItems.BLANK_GENE_CASSETTE.get());
                        output.accept(ChimeraItems.GENE_CASSETTE.get());
                        output.accept(ChimeraItems.BLANK_GENOME.get());
                        output.accept(ChimeraItems.SPLICE_CORE.get());
                        output.accept(ChimeraItems.SPLICE_CORE_MK2.get());
                        output.accept(ChimeraItems.SPLICE_CORE_MK3.get());
                        output.accept(ChimeraItems.GENE_SEQUENCER.get());
                        output.accept(ChimeraItems.GENOME_ANALYZER.get());
                        output.accept(ChimeraItems.GENE_EXTRACTOR.get());
                        output.accept(ChimeraItems.CENTRIFUGE.get());
                        output.accept(ChimeraItems.GENOME_SPLICER.get());
                        output.accept(ChimeraItems.BIOREACTOR.get());
                        output.accept(ChimeraItems.MACHINE_UPGRADE_KIT.get());
                        output.accept(ChimeraItems.SPEED_UPGRADE_1.get());
                        output.accept(ChimeraItems.SPEED_UPGRADE_2.get());
                        output.accept(ChimeraItems.SPEED_UPGRADE_3.get());
                        output.accept(ChimeraItems.YIELD_UPGRADE_1.get());
                        output.accept(ChimeraItems.YIELD_UPGRADE_2.get());
                        output.accept(ChimeraItems.YIELD_UPGRADE_3.get());
                    }).build());

    public ChimeraMod(IEventBus modEventBus, ModContainer modContainer) {
        ChimeraBlocks.BLOCKS.register(modEventBus);
        ChimeraBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ChimeraMenus.MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ChimeraItems.ITEMS.register(modEventBus);
        ChimeraDataComponents.DATA_COMPONENTS.register(modEventBus);
        ChimeraAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ChimeraMobEffects.MOB_EFFECTS.register(modEventBus);
        ChimeraPotions.POTIONS.register(modEventBus);

        modEventBus.addListener(ChimeraDataGenerators::gatherData);
        modEventBus.addListener(ChimeraPayloads::register);

        modEventBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.GENE_SEQUENCER.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.GENOME_ANALYZER.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.GENE_EXTRACTOR.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.CENTRIFUGE.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.GENOME_SPLICER.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ChimeraBlockEntities.BIOREACTOR.get(),
                    (blockEntity, side) -> blockEntity.getInventory());
        });

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new GeneRegistry());
            event.addListener(new GenePoolRegistry());
        });

        NeoForge.EVENT_BUS.register(new GeneEffectHandlers());
        NeoForge.EVENT_BUS.register(new TissueScraperEventHandler());

        // Hunt gate (v0.2 tier-2 work order Milestone 3): Awkward Potion + Adrenal Extract ->
        // Potion of Stress. Splash/Lingering variants come free from vanilla's own generic
        // container-upgrade recipes - no extra registration needed for those.
        NeoForge.EVENT_BUS.addListener((RegisterBrewingRecipesEvent event) ->
                event.getBuilder().addMix(Potions.AWKWARD, ChimeraItems.ADRENAL_EXTRACT.get(), ChimeraPotions.STRESS));

        // Deferred to common setup so registries are populated before ChimeraCuriosCompat
        // calls ChimeraItems.SPLICE_CORE.get() - Curios is a soft dependency (CLAUDE.md
        // architecture rule #5): only touch its classes if it's actually loaded, and only
        // from this one gated call site.
        modEventBus.addListener((FMLCommonSetupEvent event) -> {
            if (ModList.get().isLoaded("curios")) {
                com.chimera.curios.ChimeraCuriosCompat.register();
            }
        });
    }
}
