package com.chimera.item;

import java.util.List;

import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class TissueSampleItem extends Item {

    // Sentinel species tag for a Self Tissue Sample (Biopedia+Oath work order Milestone 2) -
    // Player isn't a real EntityType, so it's never in BuiltInRegistries.ENTITY_TYPE.
    public static final ResourceLocation PLAYER_SPECIES = ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "player");

    public TissueSampleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation species = stack.get(ChimeraDataComponents.SPECIES.get());
        if (species == null) {
            return;
        }
        // BuiltInRegistries.ENTITY_TYPE is a DefaultedRegistry (falls back to EntityType.PIG for
        // any unrecognized key instead of returning null) - containsKey() is the only way to
        // tell "not a real entity type" from "genuinely a pig". Found the hard way: the player
        // sentinel below was silently displaying "Species: Pig".
        Component speciesName;
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(species)) {
            speciesName = BuiltInRegistries.ENTITY_TYPE.get(species).getDescription();
        } else if (species.equals(PLAYER_SPECIES)) {
            speciesName = Component.translatable("tooltip.chimera.species.player");
        } else {
            speciesName = Component.literal(species.toString());
        }
        tooltipComponents.add(Component.translatable("tooltip.chimera.species", speciesName));
    }
}
