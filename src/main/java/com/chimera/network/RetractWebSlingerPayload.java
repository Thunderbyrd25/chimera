package com.chimera.network;

import com.chimera.ChimeraMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client -> server "the player left-clicked while holding a Web Slinger" signal. Carries no
// data - the server resolves which hook (if any) that player currently has active itself, never
// trusting the client for that (mirrors GrassFedUsePayload's own shape/rationale exactly).
public record RetractWebSlingerPayload() implements CustomPacketPayload {

    public static final Type<RetractWebSlingerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "retract_web_slinger"));

    public static final StreamCodec<ByteBuf, RetractWebSlingerPayload> STREAM_CODEC = StreamCodec.unit(new RetractWebSlingerPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
