package com.chimera.item;

import java.util.List;

import com.chimera.ChimeraDataComponents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class TissueSampleItem extends Item {

    public TissueSampleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation species = stack.get(ChimeraDataComponents.SPECIES.get());
        if (species == null) {
            return;
        }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(species);
        Component speciesName = entityType != null ? entityType.getDescription() : Component.literal(species.toString());
        tooltipComponents.add(Component.translatable("tooltip.chimera.species", speciesName));
    }
}
