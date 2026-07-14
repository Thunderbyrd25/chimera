package com.chimera;

import com.chimera.gene.PlayerGeneData;

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
}
