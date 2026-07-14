package com.chimera.datagen;

import com.chimera.ChimeraBlocks;
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
        addItem(ChimeraItems.SEQUENCED_GENOME, "Sequenced Genome");
        addItem(ChimeraItems.NUTRIENT_AGAR, "Nutrient Agar");
        addItem(ChimeraItems.CELL_CULTURE, "Cell Culture");
        addItem(ChimeraItems.NUCLEOTIDE_SLURRY, "Nucleotide Slurry");
        addItem(ChimeraItems.CHROMATIN_STRAND, "Chromatin Strand");
        addItem(ChimeraItems.MUTAGEN, "Mutagen");

        addBlock(ChimeraBlocks.GENE_SEQUENCER, "Gene Sequencer");

        add("tooltip.chimera.species", "Species: %s");
        add("tooltip.chimera.unidentified", "Unidentified");

        // Gene display names, keyed by gene id (see data/chimera/genes/*.json) - shown on
        // identified genomes and, later, gene cassettes.
        add("gene.chimera.bovine_vigor", "Bovine Vigor");
        add("gene.chimera.ruminant_gut", "Ruminant Gut");
        add("gene.chimera.hollow_bones", "Hollow Bones");
        add("gene.chimera.thick_fleece", "Thick Fleece");
    }
}
