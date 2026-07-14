package com.chimera.item;

import com.chimera.ChimeraDataComponents;
import com.chimera.splice.SpliceCoreMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Mk1: one cassette slot. Deliberately doesn't implement Curios' ICurioItem directly - that
// would fail to class-load without Curios on the classpath. The equip behavior lives in
// com.chimera.curios.ChimeraCuriosCompat, wired up only when Curios is present.
public class SpliceCoreItem extends Item {

    public SpliceCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new SpliceCoreMenu(containerId, playerInventory, hand),
                    Component.translatable(getDescriptionId(stack))), buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation trait = stack.get(ChimeraDataComponents.INSTALLED_TRAIT.get());
        if (trait == null) {
            return super.getName(stack);
        }
        Component traitName = Component.translatable("gene." + trait.getNamespace() + "." + trait.getPath());
        return Component.translatable("item.chimera.splice_core.named", traitName);
    }
}
