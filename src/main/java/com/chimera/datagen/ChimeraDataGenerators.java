package com.chimera.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class ChimeraDataGenerators {

    public static void gatherData(GatherDataEvent event) {
        if (event.includeClient()) {
            event.addProvider(new ChimeraItemModelProvider(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
            event.addProvider(new ChimeraBlockStateProvider(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
            event.createProvider(ChimeraLanguageProvider::new);
        }
        if (event.includeServer()) {
            event.createProvider(ChimeraRecipeProvider::new);
            event.createProvider(ChimeraLootTableProvider::new);
        }
    }
}
