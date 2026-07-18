package com.chimera.gene;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
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

    // One line per effect the trait actually has a description for (see GeneEffect#describe) -
    // drawbacks in red, everything else in gray. Effects with nothing to say (most existing
    // behaviors, until they get a "description" authored) are silently skipped, so this is
    // purely additive alongside traitLine() rather than a replacement for it.
    public static List<Component> effectDescriptionLines(GeneInstance instance) {
        Gene gene = GeneRegistry.get(instance.gene());
        if (gene == null) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        for (GeneEffect effect : gene.effects()) {
            Component description = effect.describe(instance.starLevel());
            if (description.getString().isEmpty()) {
                continue;
            }
            ChatFormatting color = effect.drawback() ? ChatFormatting.RED : ChatFormatting.GRAY;
            lines.add(description.copy().withStyle(color));
        }
        return lines;
    }
}
