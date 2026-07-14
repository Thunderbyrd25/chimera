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

// Tissue Sample + Nutrient Agar -> unidentified Sequenced Genome + a weighted random byproduct.
// Fuel is consumed once per completed cycle (not a separate burn-timer like a furnace) - simplest
// design that still matches "consumes sample + fuel" from the pipeline description. See NOTES.md.
public class GeneSequencerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_BYPRODUCT = 3;

    private static final int PROCESS_TIME = 100;

    public GeneSequencerBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.GENE_SEQUENCER.get(), pos, state, 4, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);
        if (!input.is(ChimeraItems.TISSUE_SAMPLE.get()) || !fuel.is(ChimeraItems.NUTRIENT_AGAR.get())) {
            return false;
        }
        ItemStack genome = new ItemStack(ChimeraItems.SEQUENCED_GENOME.get());
        return inventory.insertItem(SLOT_OUTPUT, genome, true).isEmpty();
    }

    @Override
    protected void process() {
        ItemStack input = inventory.extractItem(SLOT_INPUT, 1, false);
        inventory.extractItem(SLOT_FUEL, 1, false);

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
        int roll = random.nextInt(100);
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
        return new GeneSequencerMenu(containerId, playerInventory, this);
    }
}
