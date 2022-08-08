package net.minecraft.world.entity.ai.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class RandomPos
{
    private static final int RANDOM_POS_ATTEMPTS = 10;

    public static BlockPos generateRandomDirection(Random pRandom, int pHorizontalDistance, int pVerticalDistance)
    {
        int i = pRandom.nextInt(2 * pHorizontalDistance + 1) - pHorizontalDistance;
        int j = pRandom.nextInt(2 * pVerticalDistance + 1) - pVerticalDistance;
        int k = pRandom.nextInt(2 * pHorizontalDistance + 1) - pHorizontalDistance;
        return new BlockPos(i, j, k);
    }

    @Nullable
    public static BlockPos generateRandomDirectionWithinRadians(Random pRandom, int pMaxHorizontalDifference, int pYRange, int pY, double pX, double p_148559_, double pZ)
    {
        double d0 = Mth.atan2(p_148559_, pX) - (double)((float)Math.PI / 2F);
        double d1 = d0 + (double)(2.0F * pRandom.nextFloat() - 1.0F) * pZ;
        double d2 = Math.sqrt(pRandom.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)pMaxHorizontalDifference;
        double d3 = -d2 * Math.sin(d1);
        double d4 = d2 * Math.cos(d1);

        if (!(Math.abs(d3) > (double)pMaxHorizontalDifference) && !(Math.abs(d4) > (double)pMaxHorizontalDifference))
        {
            int i = pRandom.nextInt(2 * pYRange + 1) - pYRange + pY;
            return new BlockPos(d3, (double)i, d4);
        }
        else
        {
            return null;
        }
    }

    @VisibleForTesting
    public static BlockPos moveUpOutOfSolid(BlockPos pPos, int pMaxY, Predicate<BlockPos> pPosPredicate)
    {
        if (!pPosPredicate.test(pPos))
        {
            return pPos;
        }
        else
        {
            BlockPos blockpos;

            for (blockpos = pPos.above(); blockpos.getY() < pMaxY && pPosPredicate.test(blockpos); blockpos = blockpos.above())
            {
            }

            return blockpos;
        }
    }

    @VisibleForTesting
    public static BlockPos moveUpToAboveSolid(BlockPos pPos, int pAboveSolidAmount, int pMaxY, Predicate<BlockPos> pPosPredicate)
    {
        if (pAboveSolidAmount < 0)
        {
            throw new IllegalArgumentException("aboveSolidAmount was " + pAboveSolidAmount + ", expected >= 0");
        }
        else if (!pPosPredicate.test(pPos))
        {
            return pPos;
        }
        else
        {
            BlockPos blockpos;

            for (blockpos = pPos.above(); blockpos.getY() < pMaxY && pPosPredicate.test(blockpos); blockpos = blockpos.above())
            {
            }

            BlockPos blockpos1;
            BlockPos blockpos2;

            for (blockpos1 = blockpos; blockpos1.getY() < pMaxY && blockpos1.getY() - blockpos.getY() < pAboveSolidAmount; blockpos1 = blockpos2)
            {
                blockpos2 = blockpos1.above();

                if (pPosPredicate.test(blockpos2))
                {
                    break;
                }
            }

            return blockpos1;
        }
    }

    @Nullable
    public static Vec3 generateRandomPos(PathfinderMob pPosSupplier, Supplier<BlockPos> pToDoubleFunction)
    {
        return generateRandomPos(pToDoubleFunction, pPosSupplier::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 generateRandomPos(Supplier<BlockPos> pPosSupplier, ToDoubleFunction<BlockPos> pToDoubleFunction)
    {
        double d0 = Double.NEGATIVE_INFINITY;
        BlockPos blockpos = null;

        for (int i = 0; i < 10; ++i)
        {
            BlockPos blockpos1 = pPosSupplier.get();

            if (blockpos1 != null)
            {
                double d1 = pToDoubleFunction.applyAsDouble(blockpos1);

                if (d1 > d0)
                {
                    d0 = d1;
                    blockpos = blockpos1;
                }
            }
        }

        return blockpos != null ? Vec3.atBottomCenterOf(blockpos) : null;
    }

    public static BlockPos generateRandomPosTowardDirection(PathfinderMob pMob, int pRange, Random pRandom, BlockPos pPos)
    {
        int i = pPos.getX();
        int j = pPos.getZ();

        if (pMob.hasRestriction() && pRange > 1)
        {
            BlockPos blockpos = pMob.getRestrictCenter();

            if (pMob.getX() > (double)blockpos.getX())
            {
                i -= pRandom.nextInt(pRange / 2);
            }
            else
            {
                i += pRandom.nextInt(pRange / 2);
            }

            if (pMob.getZ() > (double)blockpos.getZ())
            {
                j -= pRandom.nextInt(pRange / 2);
            }
            else
            {
                j += pRandom.nextInt(pRange / 2);
            }
        }

        return new BlockPos((double)i + pMob.getX(), (double)pPos.getY() + pMob.getY(), (double)j + pMob.getZ());
    }
}
