package com.chimera.network;

import com.chimera.ChimeraMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client -> server "the player pressed the Grass Fed ability key" signal. Carries no data -
// gameplay effects must be resolved server-side (does the player actually have Grass Fed
// active, are they on cooldown, are they standing on grass), never trust the client for that.
public record GrassFedUsePayload() implements CustomPacketPayload {

    public static final Type<GrassFedUsePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "grass_fed_use"));

    public static final StreamCodec<ByteBuf, GrassFedUsePayload> STREAM_CODEC = StreamCodec.unit(new GrassFedUsePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
