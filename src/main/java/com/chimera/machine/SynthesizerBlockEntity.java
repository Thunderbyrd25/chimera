package com.chimera.machine;

import java.util.Optional;

import com.chimera.ChimeraBlockEntities;
import com.chimera.gene.GenePool;
import com.chimera.gene.GenePoolRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// Byproduct economy work order Milestone 2a: synthesizes a domesticated mob's specific byproduct
// into a chance-rolled Scrap (or, for already-granular vanilla items like Feather/String, the
// real material directly) - see GenePool.synthesisOutputs()/rollSynthesisOutput(). Fuel
// (Biomass) is handled generically by AbstractMachineBlockEntity, same as every other machine.
public class SynthesizerBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private static final int PROCESS_TIME = 100;

    public SynthesizerBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.SYNTHESIZER.get(), pos, state, 2, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        Optional<GenePool> pool = findSynthesisPool(input);
        if (pool.isEmpty()) {
            return false;
        }
        // Representative-candidate check only, using the pool's first entry - real output is
        // chosen at process() time. Known edge case for the 2-candidate pools (cow/chicken/
        // sheep): if the output slot already holds a full/mismatched stack of the *other*
        // candidate, this can pass but the real roll then silently fails to insert - input and
        // fuel are still spent that cycle. Accepted rather than engineered around, matching
        // GeneSequencerBlockEntity's own "bonus byproduct not granted if it doesn't fit" stance.
        ResourceLocation candidate = pool.get().synthesisOutputs().get(0).item();
        if (!BuiltInRegistries.ITEM.containsKey(candidate)) {
            return false;
        }
        ItemStack probe = new ItemStack(BuiltInRegistries.ITEM.get(candidate));
        return inventory.insertItem(SLOT_OUTPUT, probe, true).isEmpty();
    }

    @Override
    protected void process() {
        ItemStack input = inventory.extractItem(SLOT_INPUT, 1, false);
        RandomSource random = this.level != null ? this.level.random : RandomSource.create();
        findSynthesisPool(input).flatMap(pool -> pool.rollSynthesisOutput(random))
                .filter(BuiltInRegistries.ITEM::containsKey)
                .ifPresent(itemId -> inventory.insertItem(SLOT_OUTPUT, new ItemStack(BuiltInRegistries.ITEM.get(itemId)), false));
    }

    // Package-visible (not just private) - SynthesizerMenu's quickMoveStack reuses this to
    // recognize a shift-clicked byproduct as belonging in SLOT_INPUT, rather than duplicating
    // the reverse-lookup. No instance state involved, so this could be static; kept an instance
    // method only for symmetry with the rest of this class.
    Optional<GenePool> findSynthesisPool(ItemStack input) {
        ResourceLocation inputId = BuiltInRegistries.ITEM.getKey(input.getItem());
        for (GenePool pool : GenePoolRegistry.getAll().values()) {
            if (pool.specificByproduct().isPresent() && pool.specificByproduct().get().equals(inputId) && !pool.synthesisOutputs().isEmpty()) {
                return Optional.of(pool);
            }
        }
        return Optional.empty();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SynthesizerMenu(containerId, playerInventory, this, getUpgradeSlotCount());
    }
}
