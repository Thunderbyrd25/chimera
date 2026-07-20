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
        handheldItem(ChimeraItems.APEX_TISSUE_SCRAPER.get());
        handheldItem(ChimeraItems.PREDATOR_TISSUE_SCRAPER.get());
        basicItem(ChimeraItems.TISSUE_SAMPLE.get());
        basicItem(ChimeraItems.STRESS_PLASMA.get());
        basicItem(ChimeraItems.COMBAT_STIMULANT.get());
        basicItem(ChimeraItems.ADRENAL_EXTRACT.get());
        basicItem(ChimeraItems.SEQUENCED_GENOME.get());
        basicItem(ChimeraItems.BIOMASS.get());
        basicItem(ChimeraItems.CELL_CULTURE.get());
        basicItem(ChimeraItems.NUCLEOTIDE_SLURRY.get());
        basicItem(ChimeraItems.CHROMATIN_STRAND.get());
        basicItem(ChimeraItems.MUTAGEN.get());
        basicItem(ChimeraItems.REFINED_CULTURE.get());
        basicItem(ChimeraItems.BLANK_GENE_CASSETTE.get());
        basicItem(ChimeraItems.GENE_CASSETTE.get());
        basicItem(ChimeraItems.BLANK_GENOME.get());
        basicItem(ChimeraItems.SPLICE_CORE.get());
        basicItem(ChimeraItems.SPLICE_CORE_MK2.get());
        basicItem(ChimeraItems.SPLICE_CORE_MK3.get());
        basicItem(ChimeraItems.MACHINE_UPGRADE_KIT.get());
        basicItem(ChimeraItems.SPEED_UPGRADE_1.get());
        basicItem(ChimeraItems.SPEED_UPGRADE_2.get());
        basicItem(ChimeraItems.SPEED_UPGRADE_3.get());
        basicItem(ChimeraItems.YIELD_UPGRADE_1.get());
        basicItem(ChimeraItems.YIELD_UPGRADE_2.get());
        basicItem(ChimeraItems.YIELD_UPGRADE_3.get());
    }
}
