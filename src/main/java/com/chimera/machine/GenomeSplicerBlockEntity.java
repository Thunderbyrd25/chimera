package com.chimera.machine;

import java.util.ArrayList;
import java.util.List;

import com.chimera.ChimeraBlockEntities;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.gene.GeneInstance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// Up to 3 Sequenced Genomes + a Blank Gene Cassette -> one cassette combining every input
// genome's traits (deduped by GeneInstance.highestPerGene, same rule the Splice Core uses so
// installing the same gene twice at different levels can't downgrade it). Pairs with the
// Centrifuge, which splits a genome into several single-trait genomes that can then be fed back
// in here alongside genomes from other sources - going straight from "several genomes" to "one
// equip-ready cassette" without a separate condensing step. No fuel.
public class GenomeSplicerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_GENOME_START = 0;
    public static final int GENOME_SLOT_COUNT = 3;
    public static final int SLOT_FRAME = GENOME_SLOT_COUNT;
    public static final int SLOT_OUTPUT = GENOME_SLOT_COUNT + 1;

    private static final int PROCESS_TIME = 100;

    public GenomeSplicerBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.GENOME_SPLICER.get(), pos, state, SLOT_OUTPUT + 1, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        if (collectTraits().isEmpty()) {
            return false;
        }
        if (!inventory.getStackInSlot(SLOT_FRAME).is(ChimeraItems.BLANK_GENE_CASSETTE.get())) {
            return false;
        }
        return inventory.insertItem(SLOT_OUTPUT, new ItemStack(ChimeraItems.GENE_CASSETTE.get()), true).isEmpty();
    }

    @Override
    protected void process() {
        List<GeneInstance> combined = GeneInstance.highestPerGene(collectTraits());
        if (combined.isEmpty()) {
            return;
        }

        for (int i = 0; i < GENOME_SLOT_COUNT; i++) {
            if (!inventory.getStackInSlot(SLOT_GENOME_START + i).isEmpty()) {
                inventory.extractItem(SLOT_GENOME_START + i, 1, false);
            }
        }
        inventory.extractItem(SLOT_FRAME, 1, false);

        ItemStack cassette = new ItemStack(ChimeraItems.GENE_CASSETTE.get());
        cassette.set(ChimeraDataComponents.TRAITS.get(), combined);
        cassette.set(ChimeraDataComponents.INERT.get(), false);
        inventory.insertItem(SLOT_OUTPUT, cassette, false);
    }

    private List<GeneInstance> collectTraits() {
        List<GeneInstance> traits = new ArrayList<>();
        for (int i = 0; i < GENOME_SLOT_COUNT; i++) {
            ItemStack genome = inventory.getStackInSlot(SLOT_GENOME_START + i);
            if (genome.isEmpty()) {
                continue;
            }
            List<GeneInstance> genomeTraits = genome.get(ChimeraDataComponents.TRAITS.get());
            if (genomeTraits != null) {
                traits.addAll(genomeTraits);
            }
        }
        return traits;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GenomeSplicerMenu(containerId, playerInventory, this, getUpgradeSlotCount());
    }
}
