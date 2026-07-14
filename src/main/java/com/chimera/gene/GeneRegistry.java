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

// Genes are a registry loaded from data/chimera/genes/*.json (CLAUDE.md architecture rule #1).
// A missing or invalid file is logged and skipped rather than crashing the reload.
public class GeneRegistry extends SimpleJsonResourceReloadListener {

    private static Map<ResourceLocation, Gene> genes = Map.of();

    public GeneRegistry() {
        super(new Gson(), "genes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Gene> loaded = new HashMap<>();
        resources.forEach((id, json) -> Gene.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> ChimeraMod.LOGGER.error("Failed to load gene {}: {}", id, error))
                .ifPresent(gene -> loaded.put(id, gene)));

        genes = Map.copyOf(loaded);
        ChimeraMod.LOGGER.info("Loaded {} genes: {}", genes.size(), genes.keySet());
    }

    public static Map<ResourceLocation, Gene> getAll() {
        return genes;
    }

    public static Gene get(ResourceLocation id) {
        return genes.get(id);
    }
}
