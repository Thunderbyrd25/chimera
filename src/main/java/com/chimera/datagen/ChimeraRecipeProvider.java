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

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.NUTRIENT_AGAR.get())
                .requires(Items.WHEAT)
                .requires(Items.SUGAR)
                .unlockedBy("has_wheat", has(Items.WHEAT))
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

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ChimeraItems.NUTRIENT_AGAR.get(), 1)
                .requires(ChimeraItems.NUCLEOTIDE_SLURRY.get())
                .requires(ChimeraItems.NUCLEOTIDE_SLURRY.get())
                .unlockedBy("has_nucleotide_slurry", has(ChimeraItems.NUCLEOTIDE_SLURRY.get()))
                .save(output, "chimera:nutrient_agar_from_slurry");
    }
}
