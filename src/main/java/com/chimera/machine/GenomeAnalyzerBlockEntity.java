package com.chimera.machine;

import java.util.List;

import com.chimera.ChimeraBlockEntities;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
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

// Unidentified Sequenced Genome -> identified Sequenced Genome, with its trait rolled from
// the species' datapack-driven gene pool (CLAUDE.md architecture rule #1). No fuel.
public class GenomeAnalyzerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private static final int PROCESS_TIME = 100;

    public GenomeAnalyzerBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.GENOME_ANALYZER.get(), pos, state, 2, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        if (!input.is(ChimeraItems.SEQUENCED_GENOME.get())) {
            return false;
        }
        if (Boolean.TRUE.equals(input.get(ChimeraDataComponents.IDENTIFIED.get()))) {
            return false;
        }
        if (input.get(ChimeraDataComponents.SPECIES.get()) == null) {
            return false;
        }
        return inventory.insertItem(SLOT_OUTPUT, input.copyWithCount(1), true).isEmpty();
    }

    @Override
    protected void process() {
        ItemStack input = inventory.extractItem(SLOT_INPUT, 1, false);
        ResourceLocation species = input.get(ChimeraDataComponents.SPECIES.get());

        ItemStack identified = input.copy();
        identified.set(ChimeraDataComponents.IDENTIFIED.get(), true);
        identified.set(ChimeraDataComponents.TRAITS.get(), rollTraits(species));

        inventory.insertItem(SLOT_OUTPUT, identified, false);
    }

    private List<ResourceLocation> rollTraits(ResourceLocation species) {
        if (species == null) {
            return List.of();
        }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(species);
        if (entityType == null) {
            return List.of();
        }
        GenePool pool = GenePoolRegistry.get(entityType);
        if (pool == null) {
            return List.of();
        }
        RandomSource random = this.level != null ? this.level.random : RandomSource.create();
        ResourceLocation gene = pool.rollGene(random);
        return gene != null ? List.of(gene) : List.of();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GenomeAnalyzerMenu(containerId, playerInventory, this);
    }
}
