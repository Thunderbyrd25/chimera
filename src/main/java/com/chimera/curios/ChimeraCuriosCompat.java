package com.chimera.curios;

import java.util.Objects;

import com.chimera.ChimeraAttachments;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.gene.AttributeModifierGeneEffect;
import com.chimera.gene.Gene;
import com.chimera.gene.GeneEffect;
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
        CuriosApi.registerCurio(ChimeraItems.SPLICE_CORE.get(), new SpliceCoreCurio());
    }

    private static class SpliceCoreCurio implements ICurioItem {

        @Override
        public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
            Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
            Gene gene = geneOf(stack);
            if (gene == null) {
                return modifiers;
            }
            for (GeneEffect effect : gene.effects()) {
                if (effect instanceof AttributeModifierGeneEffect attributeEffect) {
                    modifiers.put(attributeEffect.attribute(), attributeEffect.modifier());
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

            ResourceLocation oldTrait = oldStack.get(ChimeraDataComponents.INSTALLED_TRAIT.get());
            ResourceLocation newTrait = newStack.get(ChimeraDataComponents.INSTALLED_TRAIT.get());
            if (Objects.equals(oldTrait, newTrait)) {
                return;
            }

            PlayerGeneData data = player.getData(ChimeraAttachments.PLAYER_GENE_DATA.get());
            if (oldTrait != null) {
                data = data.withGeneRemoved(oldTrait);
            }
            if (newTrait != null) {
                data = data.withGeneAdded(newTrait);
            }
            player.setData(ChimeraAttachments.PLAYER_GENE_DATA.get(), data);
        }

        private Gene geneOf(ItemStack stack) {
            ResourceLocation trait = stack.get(ChimeraDataComponents.INSTALLED_TRAIT.get());
            return trait != null ? GeneRegistry.get(trait) : null;
        }
    }
}
