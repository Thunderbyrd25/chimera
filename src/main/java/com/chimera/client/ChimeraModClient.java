package com.chimera.client;

import com.chimera.ChimeraMenus;
import com.chimera.ChimeraMod;
import com.chimera.machine.GeneSequencerScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = ChimeraMod.MODID, dist = Dist.CLIENT)
public class ChimeraModClient {

    public ChimeraModClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerScreens);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ChimeraMenus.GENE_SEQUENCER.get(), GeneSequencerScreen::new);
    }
}
