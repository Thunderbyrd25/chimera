package com.chimera.network;

import com.chimera.gene.GeneEffectHandlers;
import com.chimera.oath.OathEffects;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ChimeraPayloads {

    private ChimeraPayloads() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(GrassFedUsePayload.TYPE, GrassFedUsePayload.STREAM_CODEC,
                        (payload, context) -> GeneEffectHandlers.handleGrassFedUse(context.player()))
                .playToServer(OathResponsePayload.TYPE, OathResponsePayload.STREAM_CODEC,
                        (payload, context) -> OathEffects.handleResponse(context.player(), payload))
                // Biopedia+Oath work order Milestone 3: this handler lambda only touches
                // common-safe types (NeoForge.EVENT_BUS, the plain OpenBiopediaScreenEvent) -
                // see OpenBiopediaScreenEvent's own comment for why that indirection exists.
                .playToClient(OpenBiopediaPayload.TYPE, OpenBiopediaPayload.STREAM_CODEC,
                        (payload, context) -> NeoForge.EVENT_BUS.post(new OpenBiopediaScreenEvent(payload.entries())));
    }
}
