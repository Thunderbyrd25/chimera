package com.chimera.machine;

import com.chimera.ChimeraBlockEntities;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.gene.ByproductRoller;
import com.chimera.gene.GenePool;
import com.chimera.gene.GenePoolRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// Tissue Sample -> unidentified Sequenced Genome + a generic byproduct + (byproduct-economy work
// order Milestone 1) a specific mob-unique byproduct. Fuel (Nutrient Agar) is handled generically
// by AbstractMachineBlockEntity now that every machine requires it - this class only needs its
// own input/output/byproduct slots.
public class GeneSequencerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_BYPRODUCT_GENERIC = 2;
    public static final int SLOT_BYPRODUCT_SPECIFIC = 3;

    private static final int PROCESS_TIME = 100;

    // Byproduct economy work order Milestone 1 (dialed down per user feedback after the first
    // hands-on pass - both rolls were guaranteed every cycle, which felt too generous even for
    // the "reliable" acquisition point). Independent of yieldBias(), which only affects which
    // generic item you get, not whether you get one at all.
    private static final float GENERIC_BYPRODUCT_CHANCE = 0.5F;
    private static final float SPECIFIC_BYPRODUCT_CHANCE = 0.35F;

    public GeneSequencerBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.GENE_SEQUENCER.get(), pos, state, 4, PROCESS_TIME);
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

        // Both byproduct rolls are bonuses - if either doesn't fit (or doesn't hit), it's simply
        // not granted this cycle rather than blocking the primary genome output.
        RandomSource random = this.level != null ? this.level.random : RandomSource.create();
        if (random.nextFloat() < GENERIC_BYPRODUCT_CHANCE) {
            inventory.insertItem(SLOT_BYPRODUCT_GENERIC, ByproductRoller.rollGeneric(random, yieldBias()), false);
        }

        if (species != null && random.nextFloat() < SPECIFIC_BYPRODUCT_CHANCE) {
            ItemStack specific = rollSpecificByproduct(species);
            if (!specific.isEmpty()) {
                inventory.insertItem(SLOT_BYPRODUCT_SPECIFIC, specific, false);
            }
        }
    }

    private ItemStack rollSpecificByproduct(ResourceLocation species) {
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(species)) {
            return ItemStack.EMPTY;
        }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(species);
        GenePool pool = GenePoolRegistry.get(entityType);
        if (pool == null || pool.specificByproduct().isEmpty()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation byproductId = pool.specificByproduct().get();
        return new ItemStack(BuiltInRegistries.ITEM.get(byproductId));
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GeneSequencerMenu(containerId, playerInventory, this, getUpgradeSlotCount());
    }
}
