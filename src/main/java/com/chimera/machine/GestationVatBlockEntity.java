package com.chimera.machine;

import java.util.Optional;

import com.chimera.ChimeraBlockEntities;
import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraItems;
import com.chimera.gene.SpliceRecipe;
import com.chimera.gene.SpliceRecipeRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// The Vat cluster work order Milestone 1a: the mod's first multiblock. Two Sequenced Genomes + a
// blank DNA Egg -> a filled (tagged) DNA Egg, gated on a curated SpliceRecipe match and a small
// hollow glass-ring structure around the controller. No dedicated multiblock framework exists in
// NeoForge/vanilla (confirmed by searching the decompiled sources directly) - the structure check
// is just one more condition inside canProcess(), unthrottled, consistent with every other
// machine in this mod re-evaluating canProcess() every tick already.
public class GestationVatBlockEntity extends AbstractMachineBlockEntity {

    public static final int SLOT_GENOME_A = 0;
    public static final int SLOT_GENOME_B = 1;
    public static final int SLOT_EGG_BLANK = 2;
    public static final int SLOT_OUTPUT = 3;

    private static final int PROCESS_TIME = 200;

    // The controller's own Y level (ring only, center is the controller) and one ring at Y+1
    // (center left open - a small hollow 2-tall glass tank you can see up into) - 8 + 8 = 16
    // fixed relative offsets, reusing vanilla Glass rather than a new custom "frame" block.
    private static final int[][] RING_OFFSETS = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1, 0}, {1, 0},
            {-1, 1}, {0, 1}, {1, 1}
    };

    public GestationVatBlockEntity(BlockPos pos, BlockState state) {
        super(ChimeraBlockEntities.GESTATION_VAT.get(), pos, state, SLOT_OUTPUT + 1, PROCESS_TIME);
    }

    @Override
    protected boolean canProcess() {
        if (!isStructureFormed()) {
            return false;
        }
        ItemStack genomeA = inventory.getStackInSlot(SLOT_GENOME_A);
        ItemStack genomeB = inventory.getStackInSlot(SLOT_GENOME_B);
        ItemStack eggBlank = inventory.getStackInSlot(SLOT_EGG_BLANK);
        if (!genomeA.is(ChimeraItems.SEQUENCED_GENOME.get()) || !genomeB.is(ChimeraItems.SEQUENCED_GENOME.get())) {
            return false;
        }
        if (!eggBlank.is(ChimeraItems.DNA_EGG.get()) || eggBlank.has(ChimeraDataComponents.DNA_EGG_RESULT.get())) {
            return false;
        }
        Optional<SpliceRecipe> match = findMatch(genomeA, genomeB);
        if (match.isEmpty() || !BuiltInRegistries.ENTITY_TYPE.containsKey(match.get().result())) {
            return false;
        }
        ItemStack probe = new ItemStack(ChimeraItems.DNA_EGG.get());
        probe.set(ChimeraDataComponents.DNA_EGG_RESULT.get(), match.get().result());
        return inventory.insertItem(SLOT_OUTPUT, probe, true).isEmpty();
    }

    @Override
    protected void process() {
        ItemStack genomeA = inventory.getStackInSlot(SLOT_GENOME_A);
        ItemStack genomeB = inventory.getStackInSlot(SLOT_GENOME_B);
        Optional<SpliceRecipe> match = findMatch(genomeA, genomeB);
        if (match.isEmpty()) {
            return;
        }

        inventory.extractItem(SLOT_GENOME_A, 1, false);
        inventory.extractItem(SLOT_GENOME_B, 1, false);
        inventory.extractItem(SLOT_EGG_BLANK, 1, false);

        ItemStack filledEgg = new ItemStack(ChimeraItems.DNA_EGG.get());
        filledEgg.set(ChimeraDataComponents.DNA_EGG_RESULT.get(), match.get().result());
        inventory.insertItem(SLOT_OUTPUT, filledEgg, false);
    }

    private Optional<SpliceRecipe> findMatch(ItemStack genomeA, ItemStack genomeB) {
        ResourceLocation speciesA = genomeA.get(ChimeraDataComponents.SPECIES.get());
        ResourceLocation speciesB = genomeB.get(ChimeraDataComponents.SPECIES.get());
        if (speciesA == null || speciesB == null) {
            return Optional.empty();
        }
        return SpliceRecipeRegistry.findMatch(speciesA, speciesB);
    }

    private boolean isStructureFormed() {
        if (level == null) {
            return false;
        }
        for (int dy = 0; dy <= 1; dy++) {
            for (int[] offset : RING_OFFSETS) {
                if (!level.getBlockState(worldPosition.offset(offset[0], dy, offset[1])).is(Blocks.GLASS)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GestationVatMenu(containerId, playerInventory, this, getUpgradeSlotCount());
    }
}
