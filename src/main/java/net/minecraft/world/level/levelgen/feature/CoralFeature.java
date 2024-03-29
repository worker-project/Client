package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature extends Feature<NoneFeatureConfiguration>
{
    public CoralFeature(Codec<NoneFeatureConfiguration> p_65429_)
    {
        super(p_65429_);
    }

    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext)
    {
        Random random = pContext.random();
        WorldGenLevel worldgenlevel = pContext.level();
        BlockPos blockpos = pContext.origin();
        Optional<Block> optional = Registry.BLOCK.getTag(BlockTags.CORAL_BLOCKS).flatMap((p_204734_) ->
        {
            return p_204734_.getRandomElement(random);
        }).map(Holder::value);
        return optional.isEmpty() ? false : this.placeFeature(worldgenlevel, random, blockpos, optional.get().defaultBlockState());
    }

    protected abstract boolean placeFeature(LevelAccessor pLevel, Random pRandom, BlockPos pPos, BlockState pState);

    protected boolean placeCoralBlock(LevelAccessor pLevel, Random pRandom, BlockPos pPos, BlockState pState)
    {
        BlockPos blockpos = pPos.above();
        BlockState blockstate = pLevel.getBlockState(pPos);

        if ((blockstate.is(Blocks.WATER) || blockstate.is(BlockTags.CORALS)) && pLevel.getBlockState(blockpos).is(Blocks.WATER))
        {
            pLevel.setBlock(pPos, pState, 3);

            if (pRandom.nextFloat() < 0.25F)
            {
                Registry.BLOCK.getTag(BlockTags.CORALS).flatMap((p_204731_) ->
                {
                    return p_204731_.getRandomElement(pRandom);
                }).map(Holder::value).ifPresent((p_204720_) ->
                {
                    pLevel.setBlock(blockpos, p_204720_.defaultBlockState(), 2);
                });
            }
            else if (pRandom.nextFloat() < 0.05F)
            {
                pLevel.setBlock(blockpos, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(pRandom.nextInt(4) + 1)), 2);
            }

            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                if (pRandom.nextFloat() < 0.2F)
                {
                    BlockPos blockpos1 = pPos.relative(direction);

                    if (pLevel.getBlockState(blockpos1).is(Blocks.WATER))
                    {
                        Registry.BLOCK.getTag(BlockTags.WALL_CORALS).flatMap((p_204728_) ->
                        {
                            return p_204728_.getRandomElement(pRandom);
                        }).map(Holder::value).ifPresent((p_204725_) ->
                        {
                            BlockState blockstate1 = p_204725_.defaultBlockState();

                            if (blockstate1.hasProperty(BaseCoralWallFanBlock.FACING))
                            {
                                blockstate1 = blockstate1.setValue(BaseCoralWallFanBlock.FACING, direction);
                            }

                            pLevel.setBlock(blockpos1, blockstate1, 2);
                        });
                    }
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }
}
