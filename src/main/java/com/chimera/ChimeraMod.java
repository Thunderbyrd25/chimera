package com.chimera;

import org.slf4j.Logger;

import com.chimera.datagen.ChimeraDataGenerators;
import com.chimera.gene.GenePoolRegistry;
import com.chimera.gene.GeneRegistry;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
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
    // Items and data components own their own DeferredRegister in their respective classes
    // (ChimeraItems, ChimeraDataComponents); this class holds the registries with nothing to
    // move out yet, plus the ones - like the creative tab - that tie everything together.
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, MODID);
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
                    }).build());

    public ChimeraMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ChimeraItems.ITEMS.register(modEventBus);
        ChimeraDataComponents.DATA_COMPONENTS.register(modEventBus);

        modEventBus.addListener(ChimeraDataGenerators::gatherData);

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new GeneRegistry());
            event.addListener(new GenePoolRegistry());
        });
    }
}
