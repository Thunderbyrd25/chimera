package com.chimera.machine;

import java.util.List;

import com.chimera.ChimeraBlockEntities;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.gene.GeneInstance;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// Identified Sequenced Genome + N Blank Genomes -> N single-trait Sequenced Genomes, one per
// revealed trait, spread across the output slots. Pairs with the Genome Splicer, which takes
// several of these back in and combines them onto one cassette - so traits from different
// source genomes can be cherry-picked and recombined before being made equip-ready. No fuel.
public class CentrifugeBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_GENOME = 0;
    public static final int SLOT_FRAME = 1;
    public static final int SLOT_OUTPUT_START = 2;
    public static final int OUTPUT_SLOT_COUNT = 3;

    private static final int PROCESS_TIME = 100;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.CENTRIFUGE.get(), pos, state, SLOT_OUTPUT_START + OUTPUT_SLOT_COUNT, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        ItemStack genome = inventory.getStackInSlot(SLOT_GENOME);
        if (!genome.is(ChimeraItems.SEQUENCED_GENOME.get())) {
            return false;
        }
        if (!Boolean.TRUE.equals(genome.get(ChimeraDataComponents.IDENTIFIED.get()))) {
            return false;
        }
        List<GeneInstance> traits = genome.get(ChimeraDataComponents.TRAITS.get());
        if (traits == null || traits.isEmpty() || traits.size() > OUTPUT_SLOT_COUNT) {
            return false;
        }

        ItemStack frame = inventory.getStackInSlot(SLOT_FRAME);
        if (!frame.is(ChimeraItems.BLANK_GENOME.get()) || frame.getCount() < traits.size()) {
            return false;
        }

        for (int i = 0; i < traits.size(); i++) {
            if (!inventory.insertItem(SLOT_OUTPUT_START + i, new ItemStack(ChimeraItems.SEQUENCED_GENOME.get()), true).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void process() {
        ItemStack genome = inventory.extractItem(SLOT_GENOME, 1, false);
        List<GeneInstance> traits = genome.get(ChimeraDataComponents.TRAITS.get());
        if (traits == null || traits.isEmpty()) {
            return;
        }
        ResourceLocation species = genome.get(ChimeraDataComponents.SPECIES.get());

        inventory.extractItem(SLOT_FRAME, traits.size(), false);

        for (int i = 0; i < traits.size(); i++) {
            ItemStack split = new ItemStack(ChimeraItems.SEQUENCED_GENOME.get());
            split.set(ChimeraDataComponents.IDENTIFIED.get(), true);
            split.set(ChimeraDataComponents.TRAITS.get(), List.of(traits.get(i)));
            if (species != null) {
                split.set(ChimeraDataComponents.SPECIES.get(), species);
            }
            inventory.insertItem(SLOT_OUTPUT_START + i, split, false);
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CentrifugeMenu(containerId, playerInventory, this, getUpgradeSlotCount());
    }
}
