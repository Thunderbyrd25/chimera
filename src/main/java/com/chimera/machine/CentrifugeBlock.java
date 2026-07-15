package com.chimera.machine;

import com.chimera.ChimeraBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CentrifugeBlock extends AbstractMachineBlock {

    public CentrifugeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends AbstractMachineBlockEntity> blockEntityType() {
        return ChimeraBlockEntities.CENTRIFUGE.get();
    }
}
