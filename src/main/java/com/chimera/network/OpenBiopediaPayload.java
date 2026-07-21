package com.chimera.network;

import java.util.List;

import com.chimera.ChimeraMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Server -> client: the full Biopedia catalog for this specific player, built server-side
// (see TheBiopediaItem.use()) since DISCOVERED_GENES is server-authoritative with no client
// sync - building this client-side from a stale/default local copy would show wrong data with
// nothing to ever correct it. Every gene appears (locked or discovered); the client never needs
// to know GeneRegistry/GenePoolRegistry itself to render this.
public record OpenBiopediaPayload(List<BiopediaEntry> entries) implements CustomPacketPayload {

    public static final Type<OpenBiopediaPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "open_biopedia"));

    public static final StreamCodec<ByteBuf, OpenBiopediaPayload> STREAM_CODEC = StreamCodec.composite(
            BiopediaEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenBiopediaPayload::entries,
            OpenBiopediaPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
