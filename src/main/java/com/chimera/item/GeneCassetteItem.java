package com.chimera.item;

import java.util.List;

import com.chimera.ChimeraDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Its in-hand/inventory display name is "Gene Cassette: <Trait>" once a trait is set,
// driven entirely by the trait's own translation key - no per-trait Java branching needed.
public class GeneCassetteItem extends Item {

    public GeneCassetteItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        List<ResourceLocation> traits = stack.get(ChimeraDataComponents.TRAITS.get());
        if (traits == null || traits.isEmpty()) {
            return super.getName(stack);
        }
        ResourceLocation trait = traits.get(0);
        Component traitName = Component.translatable("gene." + trait.getNamespace() + "." + trait.getPath());
        return Component.translatable("item.chimera.gene_cassette.named", traitName);
    }
}
