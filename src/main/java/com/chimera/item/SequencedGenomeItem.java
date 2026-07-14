package com.chimera.item;

import java.util.List;

import com.chimera.ChimeraDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class SequencedGenomeItem extends Item {

    public SequencedGenomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation species = stack.get(ChimeraDataComponents.SPECIES.get());
        if (species != null) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(species);
            Component speciesName = entityType != null ? entityType.getDescription() : Component.literal(species.toString());
            tooltipComponents.add(Component.translatable("tooltip.chimera.species", speciesName));
        }

        boolean identified = Boolean.TRUE.equals(stack.get(ChimeraDataComponents.IDENTIFIED.get()));
        if (!identified) {
            tooltipComponents.add(Component.translatable("tooltip.chimera.unidentified").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            return;
        }

        List<ResourceLocation> traits = stack.get(ChimeraDataComponents.TRAITS.get());
        if (traits != null) {
            for (ResourceLocation trait : traits) {
                tooltipComponents.add(Component.translatable("gene." + trait.getNamespace() + "." + trait.getPath()).withStyle(ChatFormatting.AQUA));
            }
        }
    }
}
