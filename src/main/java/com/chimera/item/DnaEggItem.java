package com.chimera.item;

import java.util.List;

import com.chimera.ChimeraDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

// The Vat cluster work order Milestone 1a: blank vs. tagged is distinguished purely by whether
// DNA_EGG_RESULT is set (see ChimeraDataComponents) - same item id either way, mirroring how
// SequencedGenomeItem distinguishes identified/unidentified state. This milestone only needs the
// data-carrying shape (crafted blank, filled by the Gestation Vat, shown in tooltip); Milestone
// 1b turns this into a BlockItem with placement/hatch behavior.
public class DnaEggItem extends Item {

    public DnaEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation result = stack.get(ChimeraDataComponents.DNA_EGG_RESULT.get());
        if (result == null) {
            tooltipComponents.add(Component.translatable("tooltip.chimera.dna_egg.blank").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            return;
        }
        Component resultName = BuiltInRegistries.ENTITY_TYPE.containsKey(result)
                ? BuiltInRegistries.ENTITY_TYPE.get(result).getDescription()
                : Component.literal(result.toString());
        tooltipComponents.add(Component.translatable("tooltip.chimera.dna_egg.result", resultName).withStyle(ChatFormatting.AQUA));
    }
}
