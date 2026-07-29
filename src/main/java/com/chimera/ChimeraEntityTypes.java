package com.chimera;

import com.chimera.entity.WebHookEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Byproduct economy work order Milestone 3b: the mod's first custom entity. No dedicated
// DeferredRegister.createEntityTypes(...) helper exists (unlike .createBlocks()/.createItems()),
// so this mirrors ChimeraAttachments/ChimeraMenus's own DeferredRegister.create(...) shape.
public class ChimeraEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ChimeraMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<WebHookEntity>> WEB_HOOK =
            ENTITY_TYPES.register("web_hook", () -> EntityType.Builder.<WebHookEntity>of(WebHookEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(5)
                    .build("web_hook"));
}
