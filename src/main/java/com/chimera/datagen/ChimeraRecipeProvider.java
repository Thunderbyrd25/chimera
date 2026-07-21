package com.chimera.datagen;

import java.util.concurrent.CompletableFuture;

import com.chimera.ChimeraItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

public class ChimeraRecipeProvider extends RecipeProvider {

    public ChimeraRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ChimeraItems.TISSUE_SCRAPER.get())
                .pattern("I")
                .pattern("S")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.BIOMASS.get())
                .requires(Items.ROTTEN_FLESH)
                .requires(Items.SUGAR)
                .unlockedBy("has_rotten_flesh", has(Items.ROTTEN_FLESH))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.GENE_SEQUENCER.get())
                .pattern("III")
                .pattern("IFI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('F', Items.FURNACE)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.BLANK_GENE_CASSETTE.get())
                .requires(Items.IRON_INGOT)
                .requires(Items.PAPER)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.GENOME_ANALYZER.get())
                .pattern("GGG")
                .pattern("GIG")
                .pattern("GRG")
                .define('G', Items.GLASS)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.GENE_EXTRACTOR.get())
                .pattern("III")
                .pattern("IHI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('H', Items.HOPPER)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.BLANK_GENOME.get())
                .requires(Items.GLASS_BOTTLE)
                .requires(Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.CENTRIFUGE.get())
                .pattern("ICI")
                .pattern("IHI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COPPER_INGOT)
                .define('H', Items.HOPPER)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.GENOME_SPLICER.get())
                .pattern("III")
                .pattern("GHG")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .define('H', Items.HOPPER)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.SPLICE_CORE.get())
                .pattern("III")
                .pattern("IGI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        // Phase 7: the loop. Reinforced scraper trades sequencer byproducts for a better tool,
        // splice core tiers trade byproducts for more gene slots, and nucleotide slurry refines
        // back into fuel - all reasons to keep sequencing after the first gene.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ChimeraItems.REINFORCED_TISSUE_SCRAPER.get())
                .pattern("I")
                .pattern("C")
                .pattern("S")
                .define('I', Items.IRON_INGOT)
                .define('C', ChimeraItems.CELL_CULTURE.get())
                .define('S', Items.STONE)
                .unlockedBy("has_cell_culture", has(ChimeraItems.CELL_CULTURE.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.SPLICE_CORE_MK2.get())
                .pattern("CIC")
                .pattern("IGI")
                .pattern("CIC")
                .define('I', Items.IRON_INGOT)
                .define('C', ChimeraItems.CHROMATIN_STRAND.get())
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_chromatin_strand", has(ChimeraItems.CHROMATIN_STRAND.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.SPLICE_CORE_MK3.get())
                .pattern("CCC")
                .pattern("CGC")
                .pattern("CCC")
                .define('C', ChimeraItems.CHROMATIN_STRAND.get())
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_chromatin_strand", has(ChimeraItems.CHROMATIN_STRAND.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.BIOMASS.get(), 1)
                .requires(ChimeraItems.NUCLEOTIDE_SLURRY.get())
                .requires(ChimeraItems.NUCLEOTIDE_SLURRY.get())
                .unlockedBy("has_nucleotide_slurry", has(ChimeraItems.NUCLEOTIDE_SLURRY.get()))
                .save(output, "chimera:biomass_from_slurry");

        // Tiered progression (v0.2): the Bioreactor turns one of each Sequencer byproduct into
        // Refined Culture, which then crafts the tier-2 Apex Tissue Scraper.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ChimeraItems.BIOREACTOR.get())
                .pattern("DID")
                .pattern("IHI")
                .pattern("DRD")
                .define('D', Items.DIAMOND)
                .define('I', Items.IRON_INGOT)
                .define('H', Items.HOPPER)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ChimeraItems.APEX_TISSUE_SCRAPER.get())
                .requires(ChimeraItems.REFINED_CULTURE.get())
                .requires(ChimeraItems.REFINED_CULTURE.get())
                .requires(Items.DIAMOND)
                .requires(Items.IRON_INGOT)
                .unlockedBy("has_refined_culture", has(ChimeraItems.REFINED_CULTURE.get()))
                .save(output);

        // Hunt gate (v0.2 tier-2 work order Milestone 3): brewing ingredient for the Potion of
        // Stress (see ChimeraMod's RegisterBrewingRecipesEvent listener) - cheap on purpose, the
        // real gate on Stress Plasma farming is the per-mob scrape cooldown (TissueScraperItem).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.ADRENAL_EXTRACT.get())
                .requires(ChimeraItems.CELL_CULTURE.get())
                .requires(ChimeraItems.CELL_CULTURE.get())
                .requires(ChimeraItems.NUCLEOTIDE_SLURRY.get())
                .unlockedBy("has_cell_culture", has(ChimeraItems.CELL_CULTURE.get()))
                .save(output);

        // Stress Plasma only comes from scraping a Potion-of-Stress-marked mob (see
        // TissueScraperItem), refines here into Combat Stimulant, tying the resource spine to
        // Refined Culture per the work order's own suggestion.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.COMBAT_STIMULANT.get())
                .requires(ChimeraItems.STRESS_PLASMA.get())
                .requires(ChimeraItems.STRESS_PLASMA.get())
                .requires(ChimeraItems.STRESS_PLASMA.get())
                .requires(ChimeraItems.REFINED_CULTURE.get())
                .unlockedBy("has_stress_plasma", has(ChimeraItems.STRESS_PLASMA.get()))
                .save(output);

        // Tier-3 unlock, mirroring the Apex Scraper recipe shape exactly one tier up.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ChimeraItems.PREDATOR_TISSUE_SCRAPER.get())
                .requires(ChimeraItems.COMBAT_STIMULANT.get())
                .requires(ChimeraItems.COMBAT_STIMULANT.get())
                .requires(Items.DIAMOND)
                .requires(Items.IRON_INGOT)
                .unlockedBy("has_combat_stimulant", has(ChimeraItems.COMBAT_STIMULANT.get()))
                .save(output);

        // Machine upgrades (v0.2): kit grows a machine's upgrade slot count; Speed/Yield each
        // build up through 3 tiers, every tier consuming 2 of the previous one plus more raw
        // material, ending in Refined Culture - same "each tier consumes the last" shape
        // Splice Core Mk2/Mk3 already use.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.MACHINE_UPGRADE_KIT.get())
                .requires(ChimeraItems.REFINED_CULTURE.get())
                .requires(ChimeraItems.CHROMATIN_STRAND.get())
                .requires(ChimeraItems.CHROMATIN_STRAND.get())
                .requires(Items.GOLD_INGOT)
                .unlockedBy("has_refined_culture", has(ChimeraItems.REFINED_CULTURE.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.SPEED_UPGRADE_1.get())
                .requires(ChimeraItems.CHROMATIN_STRAND.get())
                .requires(Items.REDSTONE)
                .requires(Items.REDSTONE)
                .unlockedBy("has_chromatin_strand", has(ChimeraItems.CHROMATIN_STRAND.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.SPEED_UPGRADE_2.get())
                .requires(ChimeraItems.SPEED_UPGRADE_1.get())
                .requires(ChimeraItems.SPEED_UPGRADE_1.get())
                .requires(ChimeraItems.CHROMATIN_STRAND.get())
                .requires(Items.REDSTONE)
                .unlockedBy("has_speed_upgrade_1", has(ChimeraItems.SPEED_UPGRADE_1.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.SPEED_UPGRADE_3.get())
                .requires(ChimeraItems.SPEED_UPGRADE_2.get())
                .requires(ChimeraItems.SPEED_UPGRADE_2.get())
                .requires(ChimeraItems.REFINED_CULTURE.get())
                .unlockedBy("has_speed_upgrade_2", has(ChimeraItems.SPEED_UPGRADE_2.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.YIELD_UPGRADE_1.get())
                .requires(ChimeraItems.CHROMATIN_STRAND.get())
                .requires(Items.GLOWSTONE_DUST)
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_chromatin_strand", has(ChimeraItems.CHROMATIN_STRAND.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.YIELD_UPGRADE_2.get())
                .requires(ChimeraItems.YIELD_UPGRADE_1.get())
                .requires(ChimeraItems.YIELD_UPGRADE_1.get())
                .requires(ChimeraItems.CHROMATIN_STRAND.get())
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_yield_upgrade_1", has(ChimeraItems.YIELD_UPGRADE_1.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.YIELD_UPGRADE_3.get())
                .requires(ChimeraItems.YIELD_UPGRADE_2.get())
                .requires(ChimeraItems.YIELD_UPGRADE_2.get())
                .requires(ChimeraItems.REFINED_CULTURE.get())
                .unlockedBy("has_yield_upgrade_2", has(ChimeraItems.YIELD_UPGRADE_2.get()))
                .save(output);

        // Biopedia+Oath work order Milestone 2: no "ritual" crafting pattern exists anywhere in
        // this mod (Splice Core, Apex/Predator Scraper, Machine Upgrade Kit all use plain
        // crafting-table recipes) - The Oath follows the same convention.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.THE_OATH.get())
                .requires(Items.BOOK)
                .requires(ChimeraItems.SELF_TISSUE_SAMPLE.get())
                .unlockedBy("has_self_tissue_sample", has(ChimeraItems.SELF_TISSUE_SAMPLE.get()))
                .save(output);

        // Also (re)craftable directly - the spec explicitly asks that losing the book not block
        // progression.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.THE_BIOPEDIA.get())
                .requires(Items.BOOK)
                .requires(ChimeraItems.REFINED_CULTURE.get())
                .unlockedBy("has_refined_culture", has(ChimeraItems.REFINED_CULTURE.get()))
                .save(output);
    }
}
