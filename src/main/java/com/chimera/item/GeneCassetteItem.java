package com.chimera.item;

import java.util.List;

import com.chimera.ChimeraDataComponents;
import com.chimera.gene.GeneInstance;
import com.chimera.gene.TraitDisplay;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

// Its in-hand/inventory display name is "Gene Cassette: <Trait>" once a trait is set,
// driven entirely by the trait's own translation key - no per-trait Java branching needed.
// Star level shows as a tooltip line rather than in the name, to keep the name from getting
// cluttered with unicode pips.
public class GeneCassetteItem extends Item {

    public GeneCassetteItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        GeneInstance trait = installedTrait(stack);
        if (trait == null) {
            return super.getName(stack);
        }
        return Component.translatable("item.chimera.gene_cassette.named", TraitDisplay.traitName(trait.gene()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        GeneInstance trait = installedTrait(stack);
        if (trait != null) {
            tooltipComponents.add(TraitDisplay.starPips(trait.starLevel()));
        }
    }

    private GeneInstance installedTrait(ItemStack stack) {
        List<GeneInstance> traits = stack.get(ChimeraDataComponents.TRAITS.get());
        return traits != null && !traits.isEmpty() ? traits.get(0) : null;
    }
}
