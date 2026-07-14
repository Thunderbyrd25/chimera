package com.chimera.gene;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// Shared rendering for "a trait as shown to the player": star pips + translated name. Used by
// item tooltips/names (SequencedGenomeItem, GeneCassetteItem, SpliceCoreItem) and the Helix
// Analyzer screen, so the look stays consistent and only needs tuning in one place.
public final class TraitDisplay {

    private TraitDisplay() {}

    public static Component starPips(int level) {
        StringBuilder pips = new StringBuilder();
        for (int i = 1; i <= Gene.MAX_STAR_LEVEL; i++) {
            pips.append(i <= level ? '★' : '☆');
        }
        return Component.literal(pips.toString());
    }

    public static Component traitName(ResourceLocation gene) {
        return Component.translatable("gene." + gene.getNamespace() + "." + gene.getPath());
    }

    public static Component traitLine(GeneInstance instance) {
        return starPips(instance.starLevel()).copy().append(" ").append(traitName(instance.gene()));
    }
}
