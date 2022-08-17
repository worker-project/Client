package com.workerai.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class CheckSurroundingBlocks {
    protected final float partialTicks;

    public CheckSurroundingBlocks(float pPartialTicks) {
        this.partialTicks = pPartialTicks;
    }

    public abstract List<BlockPos> getSurroundingBlocks();

    public abstract boolean detectedMatchingBlock(BlockPos blockToCheck);
}
