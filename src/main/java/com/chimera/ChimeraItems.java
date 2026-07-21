package com.chimera;

import com.chimera.item.ApexTissueScraperItem;
import com.chimera.item.GeneCassetteItem;
import com.chimera.item.PredatorTissueScraperItem;
import com.chimera.item.ReinforcedTissueScraperItem;
import com.chimera.item.SequencedGenomeItem;
import com.chimera.item.SpliceCoreItem;
import com.chimera.item.TheBiopediaItem;
import com.chimera.item.TheOathItem;
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

    public static final DeferredItem<BlockItem> CENTRIFUGE = ITEMS.registerSimpleBlockItem(ChimeraBlocks.CENTRIFUGE);

    public static final DeferredItem<BlockItem> GENOME_SPLICER = ITEMS.registerSimpleBlockItem(ChimeraBlocks.GENOME_SPLICER);

    public static final DeferredItem<BlockItem> BIOREACTOR = ITEMS.registerSimpleBlockItem(ChimeraBlocks.BIOREACTOR);

    // Byproduct economy work order Milestone 2a: synthesizes a mob's specific byproduct into a
    // real vanilla material - see SynthesizerBlockEntity.
    public static final DeferredItem<BlockItem> SYNTHESIZER = ITEMS.registerSimpleBlockItem(ChimeraBlocks.SYNTHESIZER);

    public static final DeferredItem<Item> TISSUE_SCRAPER = ITEMS.register("tissue_scraper",
            () -> new TissueScraperItem(new Item.Properties().durability(32)));

    public static final DeferredItem<Item> REINFORCED_TISSUE_SCRAPER = ITEMS.register("reinforced_tissue_scraper",
            () -> new ReinforcedTissueScraperItem(new Item.Properties().durability(96)));

    public static final DeferredItem<Item> APEX_TISSUE_SCRAPER = ITEMS.register("apex_tissue_scraper",
            () -> new ApexTissueScraperItem(new Item.Properties().durability(192)));

    // Tier-3 unlock (Hunt gate, v0.2 tier-2 work order Milestone 3) - see PredatorTissueScraperItem.
    public static final DeferredItem<Item> PREDATOR_TISSUE_SCRAPER = ITEMS.register("predator_tissue_scraper",
            () -> new PredatorTissueScraperItem(new Item.Properties().durability(320)));

    public static final DeferredItem<Item> TISSUE_SAMPLE = ITEMS.register("tissue_sample",
            () -> new TissueSampleItem(new Item.Properties()));

    // Biopedia+Oath work order Milestone 2: right-click-air with any scraper (see
    // TissueScraperItem.use()). Deliberately its own item id, not TISSUE_SAMPLE, so it can't
    // accidentally enter the Sequencer/Analyzer pipeline (both gate on exact item identity).
    public static final DeferredItem<Item> SELF_TISSUE_SAMPLE = ITEMS.register("self_tissue_sample",
            () -> new TissueSampleItem(new Item.Properties()));

    // The Oath: crafted from a Book + Self Tissue Sample. Right-click opens a confirmation
    // prompt - accept sets PlayerOathData and grants THE_BIOPEDIA, decline is a no-op.
    public static final DeferredItem<Item> THE_OATH = ITEMS.register("the_oath",
            () -> new TheOathItem(new Item.Properties().stacksTo(1)));

    // Placeholder this milestone - Milestone 3 replaces this with the real paginated catalog.
    public static final DeferredItem<Item> THE_BIOPEDIA = ITEMS.register("the_biopedia",
            () -> new TheBiopediaItem(new Item.Properties().stacksTo(1)));

    // Hunt gate (v0.2 tier-2 work order Milestone 3): combat-only bonus drop from scraping a
    // mob that's actively targeting the player, or bloodied and last hurt by the player - see
    // TissueScraperItem.isCombatEligible(). Refines into Combat Stimulant.
    public static final DeferredItem<Item> STRESS_PLASMA = ITEMS.register("stress_plasma",
            () -> new Item(new Item.Properties()));

    // Crafted from Stress Plasma + Refined Culture; the ingredient for the Predator Tissue
    // Scraper, mirroring how Refined Culture itself is the ingredient for the Apex Scraper.
    public static final DeferredItem<Item> COMBAT_STIMULANT = ITEMS.register("combat_stimulant",
            () -> new Item(new Item.Properties()));

    // Brewing ingredient for the Potion of Stress (see ChimeraMod's RegisterBrewingRecipesEvent
    // listener and ChimeraPotions.STRESS) - crafted from common byproducts, since the real gate
    // on Stress Plasma farming is the per-mob scrape cooldown, not ingredient scarcity.
    public static final DeferredItem<Item> ADRENAL_EXTRACT = ITEMS.register("adrenal_extract",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SEQUENCED_GENOME = ITEMS.register("sequenced_genome",
            () -> new SequencedGenomeItem(new Item.Properties()));

    // Universal machine fuel (v0.2) - one uniform fuel item across all six machines instead of
    // a bespoke "nutrient agar" concept, made from common mob/farm byproducts.
    public static final DeferredItem<Item> BIOMASS = ITEMS.register("biomass",
            () -> new Item(new Item.Properties()));

    // Sequencer byproducts. cell_culture/chromatin_strand and mutagen are plain items with no
    // use yet beyond crafting ingredients - mutagen is intentionally inert in v0.1 (see
    // CLAUDE.md Phase 4). nucleotide_slurry refines back into biomass fuel (Phase 7), closing
    // the fuel loop.
    public static final DeferredItem<Item> CELL_CULTURE = ITEMS.register("cell_culture",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NUCLEOTIDE_SLURRY = ITEMS.register("nucleotide_slurry",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> CHROMATIN_STRAND = ITEMS.register("chromatin_strand",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> MUTAGEN = ITEMS.register("mutagen",
            () -> new Item(new Item.Properties()));

    // Bioreactor output - see BioreactorBlockEntity. Crafting ingredient for the Apex Tissue
    // Scraper, finally giving Mutagen (one of the four inputs it requires) an actual use.
    public static final DeferredItem<Item> REFINED_CULTURE = ITEMS.register("refined_culture",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BLANK_GENE_CASSETTE = ITEMS.register("blank_gene_cassette",
            () -> new Item(new Item.Properties()));

    // Frame reagent consumed by the Genome Splicer, mirroring Blank Gene Cassette's role for
    // the Extractor/Centrifuge.
    public static final DeferredItem<Item> BLANK_GENOME = ITEMS.register("blank_genome",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GENE_CASSETTE = ITEMS.register("gene_cassette",
            () -> new GeneCassetteItem(new Item.Properties()));

    public static final DeferredItem<Item> SPLICE_CORE = ITEMS.register("splice_core",
            () -> new SpliceCoreItem(new Item.Properties().stacksTo(1), 1));

    public static final DeferredItem<Item> SPLICE_CORE_MK2 = ITEMS.register("splice_core_mk2",
            () -> new SpliceCoreItem(new Item.Properties().stacksTo(1), 2));

    public static final DeferredItem<Item> SPLICE_CORE_MK3 = ITEMS.register("splice_core_mk3",
            () -> new SpliceCoreItem(new Item.Properties().stacksTo(1), 3));

    // Machine upgrades (v0.2): consumed via AbstractMachineBlock#useItemOn (kit) or dropped into
    // a machine's upgrade slots (speed/yield) - see AbstractMachineBlockEntity.
    public static final DeferredItem<Item> MACHINE_UPGRADE_KIT = ITEMS.register("machine_upgrade_kit",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SPEED_UPGRADE_1 = ITEMS.register("speed_upgrade_1",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SPEED_UPGRADE_2 = ITEMS.register("speed_upgrade_2",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SPEED_UPGRADE_3 = ITEMS.register("speed_upgrade_3",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> YIELD_UPGRADE_1 = ITEMS.register("yield_upgrade_1",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> YIELD_UPGRADE_2 = ITEMS.register("yield_upgrade_2",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> YIELD_UPGRADE_3 = ITEMS.register("yield_upgrade_3",
            () -> new Item(new Item.Properties()));

    // Byproduct economy work order Milestone 1: specific (mob-unique) byproducts, one per mob
    // with a real gene kit - see each mob's gene_pools JSON "specific_byproduct" field and
    // ByproductRoller for the generic (mob-agnostic) counterpart. Passive-mob items feed the
    // future utility/husbandry sink (M2); hostile-mob items feed the future combat sink (M3).
    public static final DeferredItem<Item> MARROW_EXTRACT = ITEMS.register("marrow_extract",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ADIPOSE_RESERVE = ITEMS.register("adipose_reserve",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> KERATIN_DOWN = ITEMS.register("keratin_down",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LANOLIN_CONCENTRATE = ITEMS.register("lanolin_concentrate",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TENDON_FIBER = ITEMS.register("tendon_fiber",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> HORN_PLATE = ITEMS.register("horn_plate",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> VESTIBULAR_GLAND = ITEMS.register("vestibular_gland",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ADRENAL_MUSK_GLAND = ITEMS.register("adrenal_musk_gland",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NECROTIC_ICHOR = ITEMS.register("necrotic_ichor",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> OSSEIN_POWDER = ITEMS.register("ossein_powder",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> CHITIN_RESIN = ITEMS.register("chitin_resin",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> VENOM_SAC = ITEMS.register("venom_sac",
            () -> new Item(new Item.Properties()));

    // Byproduct economy work order Milestone 2a: chance-rolled Synthesizer output for the
    // "chunky" vanilla materials (Leather/Beef/Porkchop/Chicken/Mutton) - assembled 4-to-1 via
    // crafting recipes (see ChimeraRecipeProvider). Small already-granular vanilla items
    // (Feather, String) skip this tier entirely and come out of the Synthesizer directly.
    public static final DeferredItem<Item> LEATHER_SCRAP = ITEMS.register("leather_scrap",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BEEF_SCRAP = ITEMS.register("beef_scrap",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> PORK_SCRAP = ITEMS.register("pork_scrap",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> CHICKEN_SCRAP = ITEMS.register("chicken_scrap",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> MUTTON_SCRAP = ITEMS.register("mutton_scrap",
            () -> new Item(new Item.Properties()));
}
