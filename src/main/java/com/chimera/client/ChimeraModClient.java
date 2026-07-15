package com.chimera.client;

import org.lwjgl.glfw.GLFW;

import com.chimera.ChimeraMenus;
import com.chimera.ChimeraMod;
import com.chimera.machine.BioreactorScreen;
import com.chimera.machine.CentrifugeScreen;
import com.chimera.machine.GeneExtractorScreen;
import com.chimera.machine.GeneSequencerScreen;
import com.chimera.machine.GenomeAnalyzerScreen;
import com.chimera.machine.GenomeSplicerScreen;
import com.chimera.network.GrassFedUsePayload;
import com.chimera.splice.SpliceCoreScreen;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = ChimeraMod.MODID, dist = Dist.CLIENT)
public class ChimeraModClient {

    public static final KeyMapping GRASS_FED_KEY =
            new KeyMapping("key.chimera.grass_fed", GLFW.GLFW_KEY_G, "key.categories.chimera");

    public ChimeraModClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ChimeraMenus.GENE_SEQUENCER.get(), GeneSequencerScreen::new);
        event.register(ChimeraMenus.GENOME_ANALYZER.get(), GenomeAnalyzerScreen::new);
        event.register(ChimeraMenus.GENE_EXTRACTOR.get(), GeneExtractorScreen::new);
        event.register(ChimeraMenus.CENTRIFUGE.get(), CentrifugeScreen::new);
        event.register(ChimeraMenus.GENOME_SPLICER.get(), GenomeSplicerScreen::new);
        event.register(ChimeraMenus.BIOREACTOR.get(), BioreactorScreen::new);
        event.register(ChimeraMenus.SPLICE_CORE.get(), SpliceCoreScreen::new);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(GRASS_FED_KEY);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        while (GRASS_FED_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new GrassFedUsePayload());
        }
    }
}
