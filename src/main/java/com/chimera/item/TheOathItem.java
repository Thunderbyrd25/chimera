package com.chimera.item;

import java.util.List;

import com.chimera.network.OpenOathPromptEvent;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

// Biopedia+Oath work order Milestone 2. Deliberately has zero references to client-only classes
// (Screen/ConfirmScreen/Minecraft) - see OpenOathPromptEvent for why: NeoForge's
// RuntimeDistCleaner rejects loading this class at all on a dedicated server if its bytecode
// references them anywhere, even inside an isClientSide-guarded branch never executed there.
public class TheOathItem extends Item {

    public TheOathItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.chimera.the_oath.1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.chimera.the_oath.2").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            NeoForge.EVENT_BUS.post(new OpenOathPromptEvent(hand));
        }
        return InteractionResultHolder.success(stack);
    }
}
