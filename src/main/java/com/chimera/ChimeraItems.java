package com.chimera;

import com.chimera.item.TissueSampleItem;
import com.chimera.item.TissueScraperItem;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChimeraMod.MODID);

    public static final DeferredItem<Item> TISSUE_SCRAPER = ITEMS.register("tissue_scraper",
            () -> new TissueScraperItem(new Item.Properties().durability(32)));

    public static final DeferredItem<Item> TISSUE_SAMPLE = ITEMS.register("tissue_sample",
            () -> new TissueSampleItem(new Item.Properties()));
}
