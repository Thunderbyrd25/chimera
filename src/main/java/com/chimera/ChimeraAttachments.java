package com.chimera;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.chimera.gene.PlayerGeneData;
import com.chimera.oath.PlayerOathData;
import com.mojang.serialization.Codec;

import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ChimeraAttachments {

    private static final Codec<Set<ResourceLocation>> RESOURCE_LOCATION_SET_CODEC =
            ResourceLocation.CODEC.listOf().xmap(Set::copyOf, List::copyOf);

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ChimeraMod.MODID);

    // Persistent and copied on death (CLAUDE.md architecture rule #4) - required test, not
    // a nice-to-have: must survive death, dimension change, and relog.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerGeneData>> PLAYER_GENE_DATA =
            ATTACHMENT_TYPES.register("player_gene_data", () -> AttachmentType.builder(() -> PlayerGeneData.EMPTY)
                    .serialize(PlayerGeneData.CODEC)
                    .copyOnDeath()
                    .build());

    // Hunt gate (v0.2 tier-2 work order Milestone 3): attached to the *scraped mob*, not the
    // player - NeoForge attachments are entity-agnostic. Game time (ticks) of the mob's last
    // successful scrape, checked in TissueScraperItem against a one-day cooldown. Default 0L
    // ("never scraped") always passes the cooldown check. No copyOnDeath - irrelevant for a
    // mob's own cooldown.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> LAST_SCRAPED_TIME =
            ATTACHMENT_TYPES.register("last_scraped_time", () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .build());

    // Biopedia+Oath work order Milestone 1: permanent per-player identity state ("thematically
    // your soul"), so copied on death like PLAYER_GENE_DATA. See com.chimera.oath.PlayerOathData.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerOathData>> PLAYER_OATH_DATA =
            ATTACHMENT_TYPES.register("player_oath_data", () -> AttachmentType.builder(() -> PlayerOathData.EMPTY)
                    .serialize(PlayerOathData.CODEC)
                    .copyOnDeath()
                    .build());

    // Which gene ids this player has discovered, for the Biopedia's locked/unlocked display -
    // kept separate from PLAYER_OATH_DATA (what you've learned vs. what you've vowed), matching
    // how PLAYER_GENE_DATA and LAST_SCRAPED_TIME are already split by concern rather than
    // bundled. Nothing writes to this yet - that lands in a later milestone, once the
    // discovery-credit-attribution question (see NOTES.md) is actually resolved.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Set<ResourceLocation>>> DISCOVERED_GENES =
            ATTACHMENT_TYPES.register("discovered_genes", () -> AttachmentType.builder(() -> Set.<ResourceLocation>of())
                    .serialize(RESOURCE_LOCATION_SET_CODEC)
                    .copyOnDeath()
                    .build());

    // Byproduct economy work order Milestone 3a: attached to the *poisoned mob*, not the player -
    // same entity-agnostic pattern as LAST_SCRAPED_TIME. Poison's own periodic damage carries no
    // source entity at all (confirmed: NeoForge's poison DamageSource is built with no attacker),
    // so "who gets the lifesteal credit" has to be tracked here instead. Default Util.NIL_UUID
    // ("nobody") always fails the credit check safely. No copyOnDeath - irrelevant once the
    // poisoned mob dies.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<UUID>> POISON_BLADE_ATTACKER =
            ATTACHMENT_TYPES.register("poison_blade_attacker", () -> AttachmentType.builder(() -> Util.NIL_UUID)
                    .serialize(UUIDUtil.CODEC)
                    .build());
}
