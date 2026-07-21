package com.chimera.network;

import com.chimera.ChimeraMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

// Client -> server "the player answered the Oath confirmation prompt" signal. Carries which
// hand held The Oath (so the server can re-check that hand specifically) and the accept/decline
// choice - gameplay effects are resolved entirely server-side in OathEffects.handleResponse,
// never trusting the client's claim alone (mirrors GrassFedUsePayload's own documented
// convention). mainHand is a plain boolean rather than encoding InteractionHand directly - it
// only has two values and ByteBufCodecs has no generic enum codec, so this avoids needing one.
public record OathResponsePayload(boolean mainHand, boolean accepted) implements CustomPacketPayload {

    public static final Type<OathResponsePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "oath_response"));

    public static final StreamCodec<ByteBuf, OathResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, OathResponsePayload::mainHand,
            ByteBufCodecs.BOOL, OathResponsePayload::accepted,
            OathResponsePayload::new);

    public OathResponsePayload(InteractionHand hand, boolean accepted) {
        this(hand == InteractionHand.MAIN_HAND, accepted);
    }

    public InteractionHand hand() {
        return mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
