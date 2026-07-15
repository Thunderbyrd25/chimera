package com.chimera.machine;

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

// Identified Sequenced Genome + Blank Gene Cassette -> Gene Cassette holding *all* of the
// genome's revealed traits (with their rolled star levels preserved), not a random single pick -
// letting the extractor drop traits on the floor made getting a specific one a double-RNG
// gamble (the genome's roll, then this machine's pick). No fuel.
public class GeneExtractorBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_GENOME = 0;
    public static final int SLOT_CASSETTE_FRAME = 1;
    public static final int SLOT_OUTPUT = 2;

    private static final int PROCESS_TIME = 100;

    public GeneExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.GENE_EXTRACTOR.get(), pos, state, 3, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        ItemStack genome = inventory.getStackInSlot(SLOT_GENOME);
        ItemStack frame = inventory.getStackInSlot(SLOT_CASSETTE_FRAME);
        if (!genome.is(ChimeraItems.SEQUENCED_GENOME.get()) || !frame.is(ChimeraItems.BLANK_GENE_CASSETTE.get())) {
            return false;
        }
        if (!Boolean.TRUE.equals(genome.get(ChimeraDataComponents.IDENTIFIED.get()))) {
            return false;
        }
        List<GeneInstance> traits = genome.get(ChimeraDataComponents.TRAITS.get());
        if (traits == null || traits.isEmpty()) {
            return false;
        }
        return inventory.insertItem(SLOT_OUTPUT, new ItemStack(ChimeraItems.GENE_CASSETTE.get()), true).isEmpty();
    }

    @Override
    protected void process() {
        ItemStack genome = inventory.extractItem(SLOT_GENOME, 1, false);
        inventory.extractItem(SLOT_CASSETTE_FRAME, 1, false);

        List<GeneInstance> traits = genome.get(ChimeraDataComponents.TRAITS.get());
        if (traits == null || traits.isEmpty()) {
            return;
        }

        ItemStack cassette = new ItemStack(ChimeraItems.GENE_CASSETTE.get());
        cassette.set(ChimeraDataComponents.TRAITS.get(), List.copyOf(traits));
        cassette.set(ChimeraDataComponents.INERT.get(), false);
        inventory.insertItem(SLOT_OUTPUT, cassette, false);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GeneExtractorMenu(containerId, playerInventory, this, getUpgradeSlotCount());
    }
}
