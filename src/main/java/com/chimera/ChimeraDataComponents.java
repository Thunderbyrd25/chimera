package com.chimera;

import java.util.List;

import com.chimera.gene.GeneInstance;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChimeraDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ChimeraMod.MODID);

    // The mob species (entity type id) a Tissue Sample or Sequenced Genome was derived from.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> SPECIES =
            DATA_COMPONENTS.registerComponentType("species", builder -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC));

    // Whether a Sequenced Genome's traits have been revealed by the Genome Analyzer. Unused until Phase 3/5.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IDENTIFIED =
            DATA_COMPONENTS.registerComponentType("identified", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    // The traits (gene + star level) carried by an item: revealed traits on an identified
    // Sequenced Genome, the one trait on a Gene Cassette, or the traits currently installed in
    // a Splice Core (0..slotCount entries depending on Mk1/Mk2/Mk3).
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<GeneInstance>>> TRAITS =
            DATA_COMPONENTS.registerComponentType("traits", builder -> builder
                    .persistent(GeneInstance.CODEC.listOf())
                    .networkSynchronized(GeneInstance.STREAM_CODEC.apply(ByteBufCodecs.list())));

    // Future hook (see CLAUDE.md architecture rule #6): unused in v0.1, always false.
    // Will mark a Gene Cassette as inert once the corruption/soul-harvesting half of the mod exists.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INERT =
            DATA_COMPONENTS.registerComponentType("inert", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    // The actual Gene Cassette ItemStacks plugged into a Splice Core, indexed by slot. Reuses
    // vanilla's own container-in-item type (same one shulker boxes/bundles use) instead of a
    // flattened trait list, so a cassette carrying multiple traits (see GeneExtractorBlockEntity)
    // keeps its per-slot identity across GUI reopen instead of being re-split by trait count.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> INSTALLED_CASSETTES =
            DATA_COMPONENTS.registerComponentType("installed_cassettes", builder -> builder
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    // The Vat cluster work order Milestone 1a: the hybrid EntityType a DNA Egg is tagged to hatch
    // into. Mirrors SPECIES's own shape (plain ResourceLocation, absence = untagged) rather than
    // an Optional wrapper - a blank egg simply has no component set, same as an un-sequenced
    // Tissue Sample has no SPECIES.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> DNA_EGG_RESULT =
            DATA_COMPONENTS.registerComponentType("dna_egg_result", builder -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC));
}
