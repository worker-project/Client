package com.workerai.utils;

import net.minecraft.core.BlockPos;

import java.util.List;

public abstract class CheckSurroundingBlocks {
    public CheckSurroundingBlocks() {
    }
    public abstract List<BlockPos> getSurroundingBlocks();

    public abstract void detectedMatchingBlock(BlockPos blockToCheck);
}
