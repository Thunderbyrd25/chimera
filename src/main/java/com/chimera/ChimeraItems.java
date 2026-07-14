package com.chimera;

import com.chimera.item.GeneCassetteItem;
import com.chimera.item.SequencedGenomeItem;
import com.chimera.item.TissueSampleItem;
import com.chimera.item.TissueScraperItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChimeraMod.MODID);

    public static final DeferredItem<BlockItem> GENE_SEQUENCER = ITEMS.registerSimpleBlockItem(ChimeraBlocks.GENE_SEQUENCER);
    public static final DeferredItem<BlockItem> GENOME_ANALYZER = ITEMS.registerSimpleBlockItem(ChimeraBlocks.GENOME_ANALYZER);
    public static final DeferredItem<BlockItem> GENE_EXTRACTOR = ITEMS.registerSimpleBlockItem(ChimeraBlocks.GENE_EXTRACTOR);

    public static final DeferredItem<Item> TISSUE_SCRAPER = ITEMS.register("tissue_scraper",
            () -> new TissueScraperItem(new Item.Properties().durability(32)));

    public static final DeferredItem<Item> TISSUE_SAMPLE = ITEMS.register("tissue_sample",
            () -> new TissueSampleItem(new Item.Properties()));

    public static final DeferredItem<Item> SEQUENCED_GENOME = ITEMS.register("sequenced_genome",
            () -> new SequencedGenomeItem(new Item.Properties()));

    public static final DeferredItem<Item> NUTRIENT_AGAR = ITEMS.register("nutrient_agar",
            () -> new Item(new Item.Properties()));

    // Sequencer byproducts. cell_culture/nucleotide_slurry/chromatin_strand and mutagen are
    // plain items with no use yet - mutagen is intentionally inert in v0.1 (see CLAUDE.md Phase 4).
    public static final DeferredItem<Item> CELL_CULTURE = ITEMS.register("cell_culture",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NUCLEOTIDE_SLURRY = ITEMS.register("nucleotide_slurry",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> CHROMATIN_STRAND = ITEMS.register("chromatin_strand",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> MUTAGEN = ITEMS.register("mutagen",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BLANK_GENE_CASSETTE = ITEMS.register("blank_gene_cassette",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GENE_CASSETTE = ITEMS.register("gene_cassette",
            () -> new GeneCassetteItem(new Item.Properties()));
}
