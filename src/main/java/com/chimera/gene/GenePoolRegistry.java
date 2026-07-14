package com.chimera.gene;

import java.util.HashMap;
import java.util.Map;

import com.chimera.ChimeraMod;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

// The mob -> trait mapping lives entirely in data/chimera/gene_pools/<mob_namespace>/<mob_path>.json
// (CLAUDE.md architecture rule #1). Adding a new mob means adding a JSON file, never touching Java.
// SimpleJsonResourceReloadListener keeps the pack namespace ("chimera") separate from the folder
// path, so a pool's key ends up as "chimera:<mob_namespace>/<mob_path>" - see get(EntityType).
public class GenePoolRegistry extends SimpleJsonResourceReloadListener {

    private static Map<ResourceLocation, GenePool> pools = Map.of();

    public GenePoolRegistry() {
        super(new Gson(), "gene_pools");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, GenePool> loaded = new HashMap<>();
        resources.forEach((id, json) -> GenePool.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> ChimeraMod.LOGGER.error("Failed to load gene pool {}: {}", id, error))
                .ifPresent(pool -> loaded.put(id, pool)));

        pools = Map.copyOf(loaded);
        ChimeraMod.LOGGER.info("Loaded {} gene pools: {}", pools.size(), pools.keySet());
    }

    public static GenePool get(EntityType<?> entityType) {
        ResourceLocation speciesId = EntityType.getKey(entityType);
        ResourceLocation poolId = ResourceLocation.fromNamespaceAndPath(
                ChimeraMod.MODID, speciesId.getNamespace() + "/" + speciesId.getPath());
        return pools.get(poolId);
    }
}
