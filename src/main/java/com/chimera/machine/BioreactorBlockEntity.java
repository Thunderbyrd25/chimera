package com.chimera.machine;

import com.chimera.ChimeraBlockEntities;
import com.chimera.ChimeraItems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// One of each Sequencer byproduct (Cell Culture, Nucleotide Slurry, Chromatin Strand, Mutagen)
// -> one Refined Culture, the ingredient for the Apex Tissue Scraper. Requiring all four at once
// means the pace is set by the rarest byproduct (Mutagen, 5% per Sequencer cycle - see
// GeneSequencerBlockEntity.rollByproduct()), not by whichever is easiest to farm. No fuel.
public class BioreactorBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_CELL_CULTURE = 0;
    public static final int SLOT_NUCLEOTIDE_SLURRY = 1;
    public static final int SLOT_CHROMATIN_STRAND = 2;
    public static final int SLOT_MUTAGEN = 3;
    public static final int SLOT_OUTPUT = 4;

    private static final int PROCESS_TIME = 200;

    public BioreactorBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.BIOREACTOR.get(), pos, state, SLOT_OUTPUT + 1, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        if (!inventory.getStackInSlot(SLOT_CELL_CULTURE).is(ChimeraItems.CELL_CULTURE.get())) {
            return false;
        }
        if (!inventory.getStackInSlot(SLOT_NUCLEOTIDE_SLURRY).is(ChimeraItems.NUCLEOTIDE_SLURRY.get())) {
            return false;
        }
        if (!inventory.getStackInSlot(SLOT_CHROMATIN_STRAND).is(ChimeraItems.CHROMATIN_STRAND.get())) {
            return false;
        }
        if (!inventory.getStackInSlot(SLOT_MUTAGEN).is(ChimeraItems.MUTAGEN.get())) {
            return false;
        }
        return inventory.insertItem(SLOT_OUTPUT, new ItemStack(ChimeraItems.REFINED_CULTURE.get()), true).isEmpty();
    }

    @Override
    protected void process() {
        inventory.extractItem(SLOT_CELL_CULTURE, 1, false);
        inventory.extractItem(SLOT_NUCLEOTIDE_SLURRY, 1, false);
        inventory.extractItem(SLOT_CHROMATIN_STRAND, 1, false);
        inventory.extractItem(SLOT_MUTAGEN, 1, false);
        inventory.insertItem(SLOT_OUTPUT, new ItemStack(ChimeraItems.REFINED_CULTURE.get()), false);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BioreactorMenu(containerId, playerInventory, this, getUpgradeSlotCount());
    }
}
