package com.chimera.network;

import com.chimera.gene.GeneEffectHandlers;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ChimeraPayloads {

    private ChimeraPayloads() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(GrassFedUsePayload.TYPE, GrassFedUsePayload.STREAM_CODEC,
                (payload, context) -> GeneEffectHandlers.handleGrassFedUse(context.player()));
    }
}
