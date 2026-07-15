package com.chimera.machine;

import com.chimera.ChimeraBlockEntities;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredItem;

// Tissue Sample -> unidentified Sequenced Genome + a weighted random byproduct. Fuel (Nutrient
// Agar) is handled generically by AbstractMachineBlockEntity now that every machine requires
// it - this class only needs its own input/output/byproduct slots.
public class GeneSequencerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_BYPRODUCT = 2;

    private static final int PROCESS_TIME = 100;

    public GeneSequencerBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.GENE_SEQUENCER.get(), pos, state, 3, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        if (!input.is(ChimeraItems.TISSUE_SAMPLE.get())) {
            return false;
        }
        ItemStack genome = new ItemStack(ChimeraItems.SEQUENCED_GENOME.get());
        return inventory.insertItem(SLOT_OUTPUT, genome, true).isEmpty();
    }

    @Override
    protected void process() {
        ItemStack input = inventory.extractItem(SLOT_INPUT, 1, false);

        ItemStack genome = new ItemStack(ChimeraItems.SEQUENCED_GENOME.get());
        ResourceLocation species = input.get(ChimeraDataComponents.SPECIES.get());
        if (species != null) {
            genome.set(ChimeraDataComponents.SPECIES.get(), species);
        }
        genome.set(ChimeraDataComponents.IDENTIFIED.get(), false);
        inventory.insertItem(SLOT_OUTPUT, genome, false);

        // Byproduct is a bonus roll - if it doesn't fit, it's simply not granted this cycle
        // rather than blocking the primary genome output.
        inventory.insertItem(SLOT_BYPRODUCT, rollByproduct(), false);
    }

    private ItemStack rollByproduct() {
        RandomSource random = this.level != null ? this.level.random : RandomSource.create();
        // Installed Yield Upgrades bias the roll toward the rarer branches below (lower roll =
        // more likely Mutagen/Chromatin Strand) - the only machine-specific use of yieldBias().
        int roll = Math.max(0, random.nextInt(100) - yieldBias());
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

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GeneSequencerMenu(containerId, playerInventory, this, getUpgradeSlotCount());
    }
}
