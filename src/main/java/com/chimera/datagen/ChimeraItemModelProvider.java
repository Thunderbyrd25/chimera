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
        handheldItem(ChimeraItems.REINFORCED_TISSUE_SCRAPER.get());
        basicItem(ChimeraItems.TISSUE_SAMPLE.get());
        basicItem(ChimeraItems.SEQUENCED_GENOME.get());
        basicItem(ChimeraItems.NUTRIENT_AGAR.get());
        basicItem(ChimeraItems.CELL_CULTURE.get());
        basicItem(ChimeraItems.NUCLEOTIDE_SLURRY.get());
        basicItem(ChimeraItems.CHROMATIN_STRAND.get());
        basicItem(ChimeraItems.MUTAGEN.get());
        basicItem(ChimeraItems.BLANK_GENE_CASSETTE.get());
        basicItem(ChimeraItems.GENE_CASSETTE.get());
        basicItem(ChimeraItems.SPLICE_CORE.get());
        basicItem(ChimeraItems.SPLICE_CORE_MK2.get());
        basicItem(ChimeraItems.SPLICE_CORE_MK3.get());
    }
}
