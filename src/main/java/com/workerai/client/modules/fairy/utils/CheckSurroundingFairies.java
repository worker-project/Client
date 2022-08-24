package com.workerai.client.modules.fairy.utils;

import com.workerai.utils.ChatUtils;
import com.workerai.utils.CheckSurroundingBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CheckSurroundingFairies extends CheckSurroundingBlocks {
    private final int detectionBoxSize = 2;
    private BlockPos prevBlockToCheck;

    public CheckSurroundingFairies() {
    }

    @Override
    public List<BlockPos> getSurroundingBlocks() {
        BlockPos playerPos = new BlockPos(Minecraft.getInstance().player.getPosition(0));

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
    public void detectedMatchingBlock(BlockPos blockToCheck) {
        for (BlockPos blockPos : getSurroundingBlocks()) {
            if (blockPos.getX() == blockToCheck.getX() && blockPos.getY() == blockToCheck.getY() && blockPos.getZ() == blockToCheck.getZ()) {
                if (!blockToCheck.equals(this.prevBlockToCheck)) {
                    ChatUtils.sendGuiMessage(
                            String.format(
                                    "§9You are close to a fairy located in [X: %s Y: %s Z: %s]",
                                    blockToCheck.getX(),
                                    blockToCheck.getY(),
                                    blockToCheck.getZ()
                            )
                    );
                    this.prevBlockToCheck = blockToCheck;
                }
                break;
            }
        }
    }

    public BlockPos getPrevBlockToCheck() {
        return prevBlockToCheck;
    }
}
