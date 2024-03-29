package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;

public class RootSystemFeature extends Feature<RootSystemConfiguration>
{
    public RootSystemFeature(Codec<RootSystemConfiguration> p_160218_)
    {
        super(p_160218_);
    }

    public boolean place(FeaturePlaceContext<RootSystemConfiguration> pContext)
    {
        WorldGenLevel worldgenlevel = pContext.level();
        BlockPos blockpos = pContext.origin();

        if (!worldgenlevel.getBlockState(blockpos).isAir())
        {
            return false;
        }
        else
        {
            Random random = pContext.random();
            BlockPos blockpos1 = pContext.origin();
            RootSystemConfiguration rootsystemconfiguration = pContext.config();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos1.mutable();

            if (placeDirtAndTree(worldgenlevel, pContext.chunkGenerator(), rootsystemconfiguration, random, blockpos$mutableblockpos, blockpos1))
            {
                placeRoots(worldgenlevel, rootsystemconfiguration, random, blockpos1, blockpos$mutableblockpos);
            }

            return true;
        }
    }

    private static boolean spaceForTree(WorldGenLevel pLevel, RootSystemConfiguration pConfig, BlockPos pPos)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

        for (int i = 1; i <= pConfig.requiredVerticalSpaceForTree; ++i)
        {
            blockpos$mutableblockpos.move(Direction.UP);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);

            if (!isAllowedTreeSpace(blockstate, i, pConfig.allowedVerticalWaterForTree))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean isAllowedTreeSpace(BlockState pState, int pY, int pAllowedVerticalWater)
    {
        if (pState.isAir())
        {
            return true;
        }
        else
        {
            int i = pY + 1;
            return i <= pAllowedVerticalWater && pState.getFluidState().is(FluidTags.WATER);
        }
    }

    private static boolean placeDirtAndTree(WorldGenLevel pLevel, ChunkGenerator pGenerator, RootSystemConfiguration pConfig, Random pRandom, BlockPos.MutableBlockPos pMutablePos, BlockPos pBasePos)
    {
        for (int i = 0; i < pConfig.rootColumnMaxHeight; ++i)
        {
            pMutablePos.move(Direction.UP);

            if (pConfig.allowedTreePosition.test(pLevel, pMutablePos) && spaceForTree(pLevel, pConfig, pMutablePos))
            {
                BlockPos blockpos = pMutablePos.below();

                if (pLevel.getFluidState(blockpos).is(FluidTags.LAVA) || !pLevel.getBlockState(blockpos).getMaterial().isSolid())
                {
                    return false;
                }

                if (pConfig.treeFeature.value().place(pLevel, pGenerator, pRandom, pMutablePos))
                {
                    placeDirt(pBasePos, pBasePos.getY() + i, pLevel, pConfig, pRandom);
                    return true;
                }
            }
        }

        return false;
    }

    private static void placeDirt(BlockPos p_198350_, int p_198351_, WorldGenLevel p_198352_, RootSystemConfiguration p_198353_, Random p_198354_)
    {
        int i = p_198350_.getX();
        int j = p_198350_.getZ();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = p_198350_.mutable();

        for (int k = p_198350_.getY(); k < p_198351_; ++k)
        {
            placeRootedDirt(p_198352_, p_198353_, p_198354_, i, j, blockpos$mutableblockpos.set(i, k, j));
        }
    }

    private static void placeRootedDirt(WorldGenLevel p_160240_, RootSystemConfiguration p_160241_, Random p_160242_, int p_160243_, int p_160244_, BlockPos.MutableBlockPos p_160245_)
    {
        int i = p_160241_.rootRadius;
        Predicate<BlockState> predicate = (p_204762_) ->
        {
            return p_204762_.is(p_160241_.rootReplaceable);
        };

        for (int j = 0; j < p_160241_.rootPlacementAttempts; ++j)
        {
            p_160245_.setWithOffset(p_160245_, p_160242_.nextInt(i) - p_160242_.nextInt(i), 0, p_160242_.nextInt(i) - p_160242_.nextInt(i));

            if (predicate.test(p_160240_.getBlockState(p_160245_)))
            {
                p_160240_.setBlock(p_160245_, p_160241_.rootStateProvider.getState(p_160242_, p_160245_), 2);
            }

            p_160245_.setX(p_160243_);
            p_160245_.setZ(p_160244_);
        }
    }

    private static void placeRoots(WorldGenLevel pLevel, RootSystemConfiguration pConfig, Random pRandom, BlockPos pBasePos, BlockPos.MutableBlockPos pMutablePos)
    {
        int i = pConfig.hangingRootRadius;
        int j = pConfig.hangingRootsVerticalSpan;

        for (int k = 0; k < pConfig.hangingRootPlacementAttempts; ++k)
        {
            pMutablePos.setWithOffset(pBasePos, pRandom.nextInt(i) - pRandom.nextInt(i), pRandom.nextInt(j) - pRandom.nextInt(j), pRandom.nextInt(i) - pRandom.nextInt(i));

            if (pLevel.isEmptyBlock(pMutablePos))
            {
                BlockState blockstate = pConfig.hangingRootStateProvider.getState(pRandom, pMutablePos);

                if (blockstate.canSurvive(pLevel, pMutablePos) && pLevel.getBlockState(pMutablePos.above()).isFaceSturdy(pLevel, pMutablePos, Direction.DOWN))
                {
                    pLevel.setBlock(pMutablePos, blockstate, 2);
                }
            }
        }
    }
}
