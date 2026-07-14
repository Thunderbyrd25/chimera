package com.chimera;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here must match the modId entry in META-INF/neoforge.mods.toml
@Mod(ChimeraMod.MODID)
public class ChimeraMod {

    public static final String MODID = "chimera";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Every registrable type goes through a DeferredRegister. See CLAUDE.md architecture rule #3.
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Phase 1 placeholder only - proves the registry/tab wiring works. Replaced by real items in Phase 2+.
    public static final DeferredItem<Item> PLACEHOLDER_ITEM =
            ITEMS.registerSimpleItem("placeholder_item", new Item.Properties());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CHIMERA_TAB = CREATIVE_MODE_TABS.register(
            "chimera_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.chimera"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> PLACEHOLDER_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(PLACEHOLDER_ITEM.get()))
                    .build());

    public ChimeraMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
