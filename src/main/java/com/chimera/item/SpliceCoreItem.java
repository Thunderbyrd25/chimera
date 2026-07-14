package com.chimera.item;

import java.util.List;

import com.chimera.ChimeraDataComponents;
import com.chimera.splice.SpliceCoreMenu;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

// Mk1/Mk2/Mk3 all share this class, differing only in slotCount. Installed traits are stored
// as a plain TRAITS list (same component sequenced_genome/gene_cassette use) rather than a
// separate single-value component, so going from 1 slot to N slots needed no data migration.
// Deliberately doesn't implement Curios' ICurioItem directly - that would fail to class-load
// without Curios on the classpath. The equip behavior lives in com.chimera.curios.ChimeraCuriosCompat,
// wired up only when Curios is present.
public class SpliceCoreItem extends Item {

    private final int slotCount;

    public SpliceCoreItem(Properties properties, int slotCount) {
        super(properties);
        this.slotCount = slotCount;
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new SpliceCoreMenu(containerId, playerInventory, hand, slotCount),
                    Component.translatable(getDescriptionId(stack))), buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) {
        List<ResourceLocation> traits = stack.get(ChimeraDataComponents.TRAITS.get());
        if (traits == null || traits.isEmpty()) {
            return super.getName(stack);
        }
        MutableComponent joined = null;
        for (ResourceLocation trait : traits) {
            Component traitName = Component.translatable("gene." + trait.getNamespace() + "." + trait.getPath());
            joined = joined == null ? traitName.copy() : joined.append(", ").append(traitName);
        }
        return Component.translatable("item.chimera.splice_core.named", joined);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        List<ResourceLocation> traits = stack.get(ChimeraDataComponents.TRAITS.get());
        int installed = traits != null ? traits.size() : 0;
        tooltipComponents.add(Component.translatable("tooltip.chimera.splice_core.slots", installed, slotCount)
                .withStyle(ChatFormatting.GRAY));
    }
}
