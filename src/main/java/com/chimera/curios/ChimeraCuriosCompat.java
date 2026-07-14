package com.chimera.curios;

import java.util.List;
import java.util.Objects;

import com.chimera.ChimeraAttachments;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.gene.AttributeModifierGeneEffect;
import com.chimera.gene.Gene;
import com.chimera.gene.GeneEffect;
import com.chimera.gene.GeneInstance;
import com.chimera.gene.GeneRegistry;
import com.chimera.gene.PlayerGeneData;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

// Every Curios class reference lives in this one class, which is only ever loaded and
// executed when ChimeraMod's constructor confirms Curios is present (ModList.isLoaded).
// This keeps SpliceCoreItem itself free of any Curios dependency, so the mod loads fine
// without Curios installed (CLAUDE.md architecture rule #5).
public final class ChimeraCuriosCompat {

    private ChimeraCuriosCompat() {}

    public static void register() {
        SpliceCoreCurio curio = new SpliceCoreCurio();
        CuriosApi.registerCurio(ChimeraItems.SPLICE_CORE.get(), curio);
        CuriosApi.registerCurio(ChimeraItems.SPLICE_CORE_MK2.get(), curio);
        CuriosApi.registerCurio(ChimeraItems.SPLICE_CORE_MK3.get(), curio);
    }

    private static class SpliceCoreCurio implements ICurioItem {

        @Override
        public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
            Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
            for (GeneInstance trait : GeneInstance.highestPerGene(installedTraits(stack))) {
                Gene gene = GeneRegistry.get(trait.gene());
                if (gene == null) {
                    continue;
                }
                for (GeneEffect effect : gene.effects()) {
                    if (effect instanceof AttributeModifierGeneEffect attributeEffect) {
                        modifiers.put(attributeEffect.attribute(), attributeEffect.scaledModifier(trait.starLevel()));
                    }
                }
            }
            return modifiers;
        }

        @Override
        public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
            updateInstalledGenes(slotContext, prevStack, stack);
        }

        @Override
        public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
            updateInstalledGenes(slotContext, stack, newStack);
        }

        private void updateInstalledGenes(SlotContext slotContext, ItemStack oldStack, ItemStack newStack) {
            // The attachment is server-authoritative; Curios calls these on both sides.
            if (!(slotContext.entity() instanceof ServerPlayer player)) {
                return;
            }

            List<GeneInstance> oldTraits = installedTraits(oldStack);
            List<GeneInstance> newTraits = installedTraits(newStack);
            if (Objects.equals(oldTraits, newTraits)) {
                return;
            }

            PlayerGeneData data = player.getData(ChimeraAttachments.PLAYER_GENE_DATA.get());
            for (GeneInstance trait : oldTraits) {
                if (!newTraits.contains(trait)) {
                    data = data.withGeneRemoved(trait);
                }
            }
            for (GeneInstance trait : newTraits) {
                if (!oldTraits.contains(trait)) {
                    data = data.withGeneAdded(trait);
                }
            }
            player.setData(ChimeraAttachments.PLAYER_GENE_DATA.get(), data);
        }

        private List<GeneInstance> installedTraits(ItemStack stack) {
            List<GeneInstance> traits = stack.get(ChimeraDataComponents.TRAITS.get());
            return traits != null ? traits : List.of();
        }
    }
}
