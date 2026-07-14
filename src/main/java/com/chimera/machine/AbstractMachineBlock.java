package com.chimera.machine;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// Shared shape for the three v0.1 machine blocks: tick the shared AbstractMachineBlockEntity
// ticker, and right-click opens whatever MenuProvider the block entity implements. Each
// concrete block only needs to supply its BlockEntityType and construct its own block entity.
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
        if (level.getBlockEntity(pos) instanceof MenuProvider menuProvider) {
            player.openMenu(menuProvider, pos);
        }
        return InteractionResult.CONSUME;
    }
}
