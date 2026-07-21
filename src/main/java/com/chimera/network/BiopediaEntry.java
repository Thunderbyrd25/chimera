package com.chimera.network;

import java.util.List;
import java.util.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

// Biopedia+Oath work order Milestone 3: server-built catalog entry, one per gene. details is
// present only when the player has discovered this gene - Optional.empty() means the client
// renders the locked/redacted form (spec: "UNIDENTIFIED", tier hint only). Plain data, not
// pre-styled Components - ChimeraModClient does the coloring client-side, so this doesn't need
// a Component StreamCodec at all.
public record BiopediaEntry(ResourceLocation geneId, int tier, Optional<BiopediaEntryDetails> details) {

    public static final StreamCodec<ByteBuf, BiopediaEntry> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BiopediaEntry::geneId,
            ByteBufCodecs.VAR_INT, BiopediaEntry::tier,
            ByteBufCodecs.optional(BiopediaEntryDetails.STREAM_CODEC), BiopediaEntry::details,
            BiopediaEntry::new);

    public record BiopediaEntryDetails(List<ResourceLocation> mobs, boolean requiresAnima,
            List<String> upsideLines, List<String> drawbackLines) {

        public static final StreamCodec<ByteBuf, BiopediaEntryDetails> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), BiopediaEntryDetails::mobs,
                ByteBufCodecs.BOOL, BiopediaEntryDetails::requiresAnima,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), BiopediaEntryDetails::upsideLines,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), BiopediaEntryDetails::drawbackLines,
                BiopediaEntryDetails::new);
    }
}
