package com.chimera.item;

import java.util.ArrayList;
import java.util.List;

import com.chimera.ChimeraDataComponents;
import com.chimera.gene.GeneInstance;
import com.chimera.gene.TraitDisplay;
import com.chimera.splice.SpliceCoreMenu;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

// Mk1/Mk2/Mk3 all share this class, differing only in slotCount. Installed cassettes (each
// possibly carrying multiple traits, see GeneExtractorBlockEntity) are stored per-slot in
// INSTALLED_CASSETTES rather than a flattened trait list, so slot identity survives GUI reopen.
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
        List<GeneInstance> traits = installedTraits(stack);
        if (traits.isEmpty()) {
            return super.getName(stack);
        }
        MutableComponent joined = null;
        for (GeneInstance trait : traits) {
            Component traitName = TraitDisplay.traitName(trait.gene());
            joined = joined == null ? traitName.copy() : joined.append(", ").append(traitName);
        }
        return Component.translatable("item.chimera.splice_core.named", joined);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        List<GeneInstance> traits = installedTraits(stack);
        int filledSlots = installedCassettes(stack).size();
        tooltipComponents.add(Component.translatable("tooltip.chimera.splice_core.slots", filledSlots, slotCount)
                .withStyle(ChatFormatting.GRAY));
        for (GeneInstance trait : traits) {
            tooltipComponents.add(TraitDisplay.traitLine(trait));
        }
    }

    private List<ItemStack> installedCassettes(ItemStack stack) {
        ItemContainerContents contents = stack.get(ChimeraDataComponents.INSTALLED_CASSETTES.get());
        if (contents == null) {
            return List.of();
        }
        List<ItemStack> cassettes = new ArrayList<>();
        contents.nonEmptyItemsCopy().forEach(cassettes::add);
        return cassettes;
    }

    private List<GeneInstance> installedTraits(ItemStack stack) {
        List<GeneInstance> traits = new ArrayList<>();
        for (ItemStack cassette : installedCassettes(stack)) {
            List<GeneInstance> cassetteTraits = cassette.get(ChimeraDataComponents.TRAITS.get());
            if (cassetteTraits != null) {
                traits.addAll(cassetteTraits);
            }
        }
        return traits;
    }
}
