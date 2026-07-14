package com.chimera.datagen;

import java.util.concurrent.CompletableFuture;

import com.chimera.ChimeraItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
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
    }
}
