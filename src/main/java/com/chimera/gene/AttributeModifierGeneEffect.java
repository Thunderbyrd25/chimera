package com.chimera.gene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

// Preferred over BehaviorGeneEffect wherever a trait can be expressed as one - attribute
// modifiers sync to the client for free and stack/remove correctly (see CLAUDE.md).
// perLevelAmount scales the modifier by star level (1-3, see Gene.MAX_STAR_LEVEL); defaults to
// 0 for effects that don't scale.
public record AttributeModifierGeneEffect(Holder<Attribute> attribute, AttributeModifier modifier, double perLevelAmount) implements GeneEffect {

    public static final String TYPE = "attribute_modifier";

    public static final MapCodec<AttributeModifierGeneEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeModifierGeneEffect::attribute),
            AttributeModifier.CODEC.fieldOf("modifier").forGetter(AttributeModifierGeneEffect::modifier),
            Codec.DOUBLE.optionalFieldOf("per_level_amount", 0.0).forGetter(AttributeModifierGeneEffect::perLevelAmount)
    ).apply(instance, AttributeModifierGeneEffect::new));

    public AttributeModifier scaledModifier(int starLevel) {
        if (perLevelAmount == 0.0) {
            return modifier;
        }
        double amount = modifier.amount() + perLevelAmount * (starLevel - 1);
        return new AttributeModifier(modifier.id(), amount, modifier.operation());
    }

    @Override
    public String type() {
        return TYPE;
    }
}
