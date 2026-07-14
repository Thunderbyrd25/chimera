package com.chimera.datagen;

import com.chimera.ChimeraItems;
import com.chimera.ChimeraMod;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ChimeraItemModelProvider extends ItemModelProvider {

    public ChimeraItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ChimeraMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        handheldItem(ChimeraItems.TISSUE_SCRAPER.get());
        basicItem(ChimeraItems.TISSUE_SAMPLE.get());
        basicItem(ChimeraItems.SEQUENCED_GENOME.get());
        basicItem(ChimeraItems.NUTRIENT_AGAR.get());
        basicItem(ChimeraItems.CELL_CULTURE.get());
        basicItem(ChimeraItems.NUCLEOTIDE_SLURRY.get());
        basicItem(ChimeraItems.CHROMATIN_STRAND.get());
        basicItem(ChimeraItems.MUTAGEN.get());
    }
}
