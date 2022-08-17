package com.workerai.client.modules.fairy.utils;

import com.workerai.utils.ChatDebug;
import com.workerai.utils.CheckSurroundingBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CheckSurroundingFairies extends CheckSurroundingBlocks {
    private final int detectionBoxSize = 2;

    public CheckSurroundingFairies(float pPartialTicks) {
        super(pPartialTicks);
    }

    @Override
    public List<BlockPos> getSurroundingBlocks() {
        BlockPos playerPos = new BlockPos(Minecraft.getInstance().player.getPosition(super.partialTicks));

        List<BlockPos> surroundingBlocks = new ArrayList<>();

        for (int x = playerPos.getX() - detectionBoxSize; x <= playerPos.getX() + detectionBoxSize; x++) {
            for (int y = playerPos.getY() - detectionBoxSize; y <= playerPos.getY() + detectionBoxSize; y++) {
                for (int z = playerPos.getZ() - detectionBoxSize; z <= playerPos.getZ() + detectionBoxSize; z++) {
                    surroundingBlocks.add(new BlockPos(x, y, z));
                }
            }
        }

        return surroundingBlocks;
    }

    @Override
    public boolean detectedMatchingBlock(BlockPos blockToCheck) {
        for (BlockPos blockPos : getSurroundingBlocks()) {
            if (blockPos.getX() == blockToCheck.getX() && blockPos.getY() == blockToCheck.getY() && blockPos.getZ() == blockToCheck.getZ()) {
                ChatDebug.sendGuiMessage(
                        String.format(
                                "§9You are close to a fairy located in [X: %s Y: %s Z: %s]",
                                blockToCheck.getX(),
                                blockToCheck.getY(),
                                blockToCheck.getZ()
                        )
                );
                return true;
            }
        }
        return false;
    }
}
