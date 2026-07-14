package com.chimera.gene;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

// Preferred over BehaviorGeneEffect wherever a trait can be expressed as one - attribute
// modifiers sync to the client for free and stack/remove correctly (see CLAUDE.md).
public record AttributeModifierGeneEffect(Holder<Attribute> attribute, AttributeModifier modifier) implements GeneEffect {

    public static final String TYPE = "attribute_modifier";

    public static final MapCodec<AttributeModifierGeneEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeModifierGeneEffect::attribute),
            AttributeModifier.CODEC.fieldOf("modifier").forGetter(AttributeModifierGeneEffect::modifier)
    ).apply(instance, AttributeModifierGeneEffect::new));

    @Override
    public String type() {
        return TYPE;
    }
}
