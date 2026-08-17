package com.chimera.gene;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.chimera.ChimeraMod;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

// The Vat cluster work order Milestone 1a: splice recipes loaded from
// data/chimera/splice_recipes/*.json, mirroring GeneRegistry's shape (keyed by the JSON file's
// own id, not reconstructed from either parent species - a recipe isn't "owned" by one side).
public class SpliceRecipeRegistry extends SimpleJsonResourceReloadListener {

    private static Map<ResourceLocation, SpliceRecipe> recipes = Map.of();

    public SpliceRecipeRegistry() {
        super(new Gson(), "splice_recipes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, SpliceRecipe> loaded = new HashMap<>();
        resources.forEach((id, json) -> SpliceRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> ChimeraMod.LOGGER.error("Failed to load splice recipe {}: {}", id, error))
                .ifPresent(recipe -> loaded.put(id, recipe)));

        recipes = Map.copyOf(loaded);
        ChimeraMod.LOGGER.info("Loaded {} splice recipes: {}", recipes.size(), recipes.keySet());
    }

    public static Map<ResourceLocation, SpliceRecipe> getAll() {
        return recipes;
    }

    public static Optional<SpliceRecipe> findMatch(ResourceLocation speciesA, ResourceLocation speciesB) {
        return recipes.values().stream().filter(recipe -> recipe.matches(speciesA, speciesB)).findFirst();
    }
}
