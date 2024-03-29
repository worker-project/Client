package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class FallingBlock extends Block implements Fallable
{
    public FallingBlock(BlockBehaviour.Properties p_53205_)
    {
        super(p_53205_);
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        pLevel.scheduleTick(pPos, this, this.getDelayAfterPlace());
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        pLevel.scheduleTick(pCurrentPos, this, this.getDelayAfterPlace());
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        if (isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= pLevel.getMinBuildHeight())
        {
            FallingBlockEntity fallingblockentity = FallingBlockEntity.fall(pLevel, pPos, pState);
            this.falling(fallingblockentity);
        }
    }

    protected void falling(FallingBlockEntity pEntity)
    {
    }

    protected int getDelayAfterPlace()
    {
        return 2;
    }

    public static boolean isFree(BlockState pState)
    {
        Material material = pState.getMaterial();
        return pState.isAir() || pState.is(BlockTags.FIRE) || material.isLiquid() || material.isReplaceable();
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        if (pRand.nextInt(16) == 0)
        {
            BlockPos blockpos = pPos.below();

            if (isFree(pLevel.getBlockState(blockpos)))
            {
                double d0 = (double)pPos.getX() + pRand.nextDouble();
                double d1 = (double)pPos.getY() - 0.05D;
                double d2 = (double)pPos.getZ() + pRand.nextDouble();
                pLevel.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, pState), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public int getDustColor(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return -16777216;
    }
}
