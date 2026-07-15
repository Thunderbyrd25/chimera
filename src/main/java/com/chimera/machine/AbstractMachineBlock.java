package com.chimera.machine;

import javax.annotation.Nullable;

import com.chimera.ChimeraItems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// Shared shape for all six machine blocks: tick the shared AbstractMachineBlockEntity ticker,
// right-click with a Machine Upgrade Kit grows the upgrade slot count, right-click with
// anything else opens whatever MenuProvider the block entity implements. Each concrete block
// only needs to supply its BlockEntityType and construct its own block entity.
//
// The menu-open packet's extra data carries upgradeSlotCount alongside the BlockPos (instead of
// the simpler player.openMenu(MenuProvider, BlockPos) overload) because plain BlockEntity NBT
// isn't synced to the client automatically - if the client's IContainerFactory instead read
// upgradeSlotCount off its own (potentially stale) copy of the block entity, a freshly-upgraded
// machine's client and server Menus could add different numbers of slots and crash on the next
// container-content sync packet.
public abstract class AbstractMachineBlock extends Block implements EntityBlock {

    protected AbstractMachineBlock(Properties properties) {
        super(properties);
    }

    protected abstract BlockEntityType<? extends AbstractMachineBlockEntity> blockEntityType();

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != blockEntityType()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<AbstractMachineBlockEntity>) AbstractMachineBlockEntity::tick;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity machine) {
            int upgradeSlotCount = machine.getUpgradeSlotCount();
            player.openMenu((MenuProvider) machine, buf -> {
                buf.writeBlockPos(pos);
                buf.writeVarInt(upgradeSlotCount);
            });
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ChimeraItems.MACHINE_UPGRADE_KIT.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(true);
        }
        if (!(level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity machine) || !machine.tryApplyUpgradeKit()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        stack.shrink(1);
        return ItemInteractionResult.sidedSuccess(false);
    }
}
