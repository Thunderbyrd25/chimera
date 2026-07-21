package com.chimera.network;

import java.util.List;

import net.neoforged.bus.api.Event;

// Biopedia+Oath work order Milestone 3. Same bridging pattern as OpenOathPromptEvent (see its
// own comment for why): OpenBiopediaPayload's playToClient handler in ChimeraPayloads.register()
// only ever touches common-safe types, posting this plain event instead of opening the screen
// directly - the actual BookViewScreen usage lives entirely in ChimeraModClient, the one place
// safe to reference client-only classes.
public class OpenBiopediaScreenEvent extends Event {

    private final List<BiopediaEntry> entries;

    public OpenBiopediaScreenEvent(List<BiopediaEntry> entries) {
        this.entries = entries;
    }

    public List<BiopediaEntry> entries() {
        return entries;
    }
}
