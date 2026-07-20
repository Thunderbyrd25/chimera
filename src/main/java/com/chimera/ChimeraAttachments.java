package com.chimera;

import com.chimera.gene.PlayerGeneData;
import com.mojang.serialization.Codec;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ChimeraAttachments {

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
}
