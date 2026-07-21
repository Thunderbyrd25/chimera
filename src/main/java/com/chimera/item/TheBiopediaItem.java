package com.chimera.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

// Biopedia+Oath work order Milestone 2: placeholder only, granted on taking the Oath so the
// milestone's checkpoint has something concrete to give. Milestone 3 replaces this with the
// real paginated catalog screen reading GeneRegistry/DISCOVERED_GENES.
public class TheBiopediaItem extends Item {

    public TheBiopediaItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.chimera.the_biopedia.placeholder").withStyle(ChatFormatting.GRAY));
    }
}
