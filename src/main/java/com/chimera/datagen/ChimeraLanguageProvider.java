package com.chimera.datagen;

import com.chimera.ChimeraItems;
import com.chimera.ChimeraMod;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ChimeraLanguageProvider extends LanguageProvider {

    public ChimeraLanguageProvider(PackOutput output) {
        super(output, ChimeraMod.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.chimera", "Chimera");
        addItem(ChimeraItems.TISSUE_SCRAPER, "Tissue Scraper");
        addItem(ChimeraItems.TISSUE_SAMPLE, "Tissue Sample");
        add("tooltip.chimera.species", "Species: %s");
    }
}
