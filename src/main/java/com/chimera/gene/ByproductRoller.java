package com.chimera.gene;

import com.chimera.ChimeraItems;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

// Byproduct-economy work order Milestone 1: the generic (mob-agnostic) byproduct roll, shared
// between GeneSequencerBlockEntity (sequencing) and TissueScraperItem (scraping) - previously
// private to the Sequencer alone.
public final class ByproductRoller {

    private ByproductRoller() {
    }

    // yieldBias: installed Yield Upgrade bias (see AbstractMachineBlockEntity#yieldBias) biasing
    // the roll toward the rarer branches below - pass 0 for call sites with no upgrade concept
    // (e.g. a hand tool).
    public static ItemStack rollGeneric(RandomSource random, int yieldBias) {
        int roll = Math.max(0, random.nextInt(100) - yieldBias);
        DeferredItem<Item> chosen;
        if (roll < 5) {
            chosen = ChimeraItems.MUTAGEN;
        } else if (roll < 20) {
            chosen = ChimeraItems.CHROMATIN_STRAND;
        } else if (roll < 60) {
            chosen = ChimeraItems.CELL_CULTURE;
        } else {
            chosen = ChimeraItems.NUCLEOTIDE_SLURRY;
        }
        return new ItemStack(chosen.get());
    }
}
