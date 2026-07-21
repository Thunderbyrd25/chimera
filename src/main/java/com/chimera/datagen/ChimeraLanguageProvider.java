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
        addItem(ChimeraItems.REINFORCED_TISSUE_SCRAPER, "Reinforced Tissue Scraper");
        addItem(ChimeraItems.APEX_TISSUE_SCRAPER, "Apex Tissue Scraper");
        addItem(ChimeraItems.PREDATOR_TISSUE_SCRAPER, "Predator Tissue Scraper");
        addItem(ChimeraItems.TISSUE_SAMPLE, "Tissue Sample");
        addItem(ChimeraItems.SELF_TISSUE_SAMPLE, "Self Tissue Sample");
        addItem(ChimeraItems.THE_OATH, "The Oath");
        addItem(ChimeraItems.THE_BIOPEDIA, "The Biopedia");
        addItem(ChimeraItems.STRESS_PLASMA, "Stress Plasma");
        addItem(ChimeraItems.COMBAT_STIMULANT, "Combat Stimulant");
        addItem(ChimeraItems.ADRENAL_EXTRACT, "Adrenal Extract");
        addItem(ChimeraItems.SEQUENCED_GENOME, "Sequenced Genome");
        addItem(ChimeraItems.BIOMASS, "Biomass");
        addItem(ChimeraItems.CELL_CULTURE, "Cell Culture");
        addItem(ChimeraItems.NUCLEOTIDE_SLURRY, "Nucleotide Slurry");
        addItem(ChimeraItems.CHROMATIN_STRAND, "Chromatin Strand");
        addItem(ChimeraItems.MUTAGEN, "Mutagen");
        addItem(ChimeraItems.REFINED_CULTURE, "Refined Culture");
        addItem(ChimeraItems.BLANK_GENE_CASSETTE, "Blank Gene Cassette");
        addItem(ChimeraItems.GENE_CASSETTE, "Gene Cassette");
        addItem(ChimeraItems.BLANK_GENOME, "Blank Genome");
        addItem(ChimeraItems.SPLICE_CORE, "Splice Core");
        addItem(ChimeraItems.SPLICE_CORE_MK2, "Splice Core Mk2");
        addItem(ChimeraItems.SPLICE_CORE_MK3, "Splice Core Mk3");
        addItem(ChimeraItems.MACHINE_UPGRADE_KIT, "Machine Upgrade Kit");
        addItem(ChimeraItems.SPEED_UPGRADE_1, "Speed Upgrade I");
        addItem(ChimeraItems.SPEED_UPGRADE_2, "Speed Upgrade II");
        addItem(ChimeraItems.SPEED_UPGRADE_3, "Speed Upgrade III");
        addItem(ChimeraItems.YIELD_UPGRADE_1, "Yield Upgrade I");
        addItem(ChimeraItems.YIELD_UPGRADE_2, "Yield Upgrade II");
        addItem(ChimeraItems.YIELD_UPGRADE_3, "Yield Upgrade III");

        addBlock(ChimeraBlocks.GENE_SEQUENCER, "Gene Sequencer");
        addBlock(ChimeraBlocks.GENOME_ANALYZER, "Genome Analyzer");
        addBlock(ChimeraBlocks.GENE_EXTRACTOR, "Gene Extractor");
        addBlock(ChimeraBlocks.CENTRIFUGE, "Centrifuge");
        addBlock(ChimeraBlocks.GENOME_SPLICER, "Genome Splicer");
        addBlock(ChimeraBlocks.BIOREACTOR, "Bioreactor");

        add("tooltip.chimera.species", "Species: %s");
        add("tooltip.chimera.species.player", "You");
        add("tooltip.chimera.unidentified", "Unidentified");
        add("item.chimera.gene_cassette.named", "Gene Cassette: %s");
        add("item.chimera.splice_core.named", "Splice Core: %s");
        add("tooltip.chimera.splice_core.slots", "%s / %s gene slots filled");
        add("gui.chimera.genome_analyzer.analyzing", "Analyzing...");
        add("message.chimera.scrape_cooldown", "This creature needs time to recover before it can be scraped again.");

        // Biopedia+Oath work order Milestone 2. Clinical, lab-ethics tone throughout - "the
        // mysticism budget is spent later, on the break" (see chimera-ROADMAP.md).
        add("tooltip.chimera.the_oath.1", "A pledge, written in your own hand: to study life without causing it needless harm.");
        add("tooltip.chimera.the_oath.2", "Right-click to read and decide.");
        add("screen.chimera.the_oath.title", "The Oath");
        add("screen.chimera.the_oath.message",
                "I pledge to study life without causing it needless harm. This vow will shape how I work, and is not undone lightly. Do you accept?");
        add("tooltip.chimera.the_biopedia.placeholder", "A record of what you have learned. (Under construction.)");

        // Potion of Stress (Hunt gate, Milestone 3) - MobEffect display name, plus the potion
        // item's name across all three container forms (Potion.getName() keys off the vanilla
        // container item's own description id, not our mod namespace - see Potion.java).
        add("effect.chimera.stressed", "Stressed");
        add("item.minecraft.potion.effect.stress", "Potion of Stress");
        add("item.minecraft.splash_potion.effect.stress", "Splash Potion of Stress");
        add("item.minecraft.lingering_potion.effect.stress", "Lingering Potion of Stress");

        // Gene display names, keyed by gene id (see data/chimera/genes/*.json) - shown on
        // identified genomes and, later, gene cassettes.
        add("gene.chimera.bovine_vigor", "Bovine Vigor");
        add("gene.chimera.ruminant_gut", "Ruminant Gut");
        add("gene.chimera.hollow_bones", "Hollow Bones");
        add("gene.chimera.thick_fleece", "Thick Fleece");
        add("gene.chimera.grass_fed", "Grass Fed");
        add("gene.chimera.raging_bull", "Raging Bull");
        add("gene.chimera.equine_gait", "Equine Gait");
        add("gene.chimera.undying_hunger", "Undying Hunger");
        add("gene.chimera.steady_aim", "Steady Aim");
        add("gene.chimera.arachnid_climb", "Arachnid Climb");
        add("gene.chimera.pack_instinct", "Pack Instinct");
        add("gene.chimera.ramming_charge", "Ramming Charge");
        add("gene.chimera.silent_step", "Silent Step");
        add("gene.chimera.venom_glands", "Venom Glands");

        add("key.categories.chimera", "Chimera");
        add("key.chimera.grass_fed", "Grass Fed Ability");
    }
}
