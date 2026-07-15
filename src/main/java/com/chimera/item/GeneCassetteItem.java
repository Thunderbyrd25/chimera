package com.chimera.item;

import java.util.List;

import com.chimera.ChimeraDataComponents;
import com.chimera.gene.GeneInstance;
import com.chimera.gene.TraitDisplay;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

// Its in-hand/inventory display name is "Gene Cassette: <Trait>" once traits are set, driven
// entirely by each trait's own translation key - no per-trait Java branching needed. A cassette
// can carry more than one trait once condensed from a multi-trait genome (see
// GeneExtractorBlockEntity), so the name joins all installed trait names and the tooltip lists
// one star-pip line per trait, same pattern SpliceCoreItem uses for its own multi-trait name.
public class GeneCassetteItem extends Item {

    public GeneCassetteItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        List<GeneInstance> traits = installedTraits(stack);
        if (traits.isEmpty()) {
            return super.getName(stack);
        }
        MutableComponent joined = null;
        for (GeneInstance trait : traits) {
            Component traitName = TraitDisplay.traitName(trait.gene());
            joined = joined == null ? traitName.copy() : joined.append(", ").append(traitName);
        }
        return Component.translatable("item.chimera.gene_cassette.named", joined);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        for (GeneInstance trait : installedTraits(stack)) {
            tooltipComponents.add(TraitDisplay.traitLine(trait));
        }
    }

    private List<GeneInstance> installedTraits(ItemStack stack) {
        List<GeneInstance> traits = stack.get(ChimeraDataComponents.TRAITS.get());
        return traits != null ? traits : List.of();
    }
}
