package com.chimera.gene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

// The Vat cluster work order Milestone 1a: a curated (not procedural) parent-species pair -> one
// specific hybrid EntityType id. parentA/parentB are unordered - see SpliceRecipeRegistry.findMatch.
public record SpliceRecipe(ResourceLocation parentA, ResourceLocation parentB, ResourceLocation result) {

    public static final Codec<SpliceRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("parent_a").forGetter(SpliceRecipe::parentA),
            ResourceLocation.CODEC.fieldOf("parent_b").forGetter(SpliceRecipe::parentB),
            ResourceLocation.CODEC.fieldOf("result").forGetter(SpliceRecipe::result)
    ).apply(instance, SpliceRecipe::new));

    public boolean matches(ResourceLocation speciesA, ResourceLocation speciesB) {
        return (parentA.equals(speciesA) && parentB.equals(speciesB)) || (parentA.equals(speciesB) && parentB.equals(speciesA));
    }
}
