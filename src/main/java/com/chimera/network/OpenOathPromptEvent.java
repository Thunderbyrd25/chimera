package com.chimera.network;

import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.Event;

// Biopedia+Oath work order Milestone 2. Bridges TheOathItem.use() (a common class, loaded on
// both sides) to the client-only screen-opening code in ChimeraModClient, without TheOathItem's
// own bytecode ever referencing Screen/ConfirmScreen/Minecraft directly - NeoForge's
// RuntimeDistCleaner rejects loading a class on a dedicated server if its bytecode references
// client-only symbols ANYWHERE, even inside an isClientSide-guarded branch that's never
// executed there (confirmed the hard way via ./gradlew runServer - see NOTES.md). This plain
// event (no client-only types) is posted from common code and only ever has a listener
// registered inside ChimeraModClient, which is itself never loaded on a dedicated server at
// all thanks to its @Mod(dist = Dist.CLIENT) annotation.
public class OpenOathPromptEvent extends Event {

    private final InteractionHand hand;

    public OpenOathPromptEvent(InteractionHand hand) {
        this.hand = hand;
    }

    public InteractionHand hand() {
        return hand;
    }
}
