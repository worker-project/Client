package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Beardifier implements DensityFunctions.BeardifierOrMarker
{
    public static final int BEARD_KERNEL_RADIUS = 12;
    private static final int BEARD_KERNEL_SIZE = 24;
    private static final float[] BEARD_KERNEL = Util.make(new float[13824], (p_158082_) ->
    {
        for (int i = 0; i < 24; ++i)
        {
            for (int j = 0; j < 24; ++j)
            {
                for (int k = 0; k < 24; ++k)
                {
                    p_158082_[i * 24 * 24 + j * 24 + k] = (float)computeBeardContribution(j - 12, k - 12, i - 12);
                }
            }
        }
    });
    private final ObjectList<StructurePiece> rigids;
    private final ObjectList<JigsawJunction> junctions;
    private final ObjectListIterator<StructurePiece> pieceIterator;
    private final ObjectListIterator<JigsawJunction> junctionIterator;

    protected Beardifier(StructureFeatureManager pStructureFeatureManager, ChunkAccess pChunk)
    {
        ChunkPos chunkpos = pChunk.getPos();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        this.junctions = new ObjectArrayList<>(32);
        this.rigids = new ObjectArrayList<>(10);
        pStructureFeatureManager.startsForFeature(SectionPos.bottomOf(pChunk), (p_208202_) ->
        {
            return p_208202_.adaptNoise;
        }).forEach((p_208198_) ->
        {
            for (StructurePiece structurepiece : p_208198_.getPieces())
            {
                if (structurepiece.isCloseToChunk(chunkpos, 12))
                {
                    if (structurepiece instanceof PoolElementStructurePiece)
                    {
                        PoolElementStructurePiece poolelementstructurepiece = (PoolElementStructurePiece)structurepiece;
                        StructureTemplatePool.Projection structuretemplatepool$projection = poolelementstructurepiece.getElement().getProjection();

                        if (structuretemplatepool$projection == StructureTemplatePool.Projection.RIGID)
                        {
                            this.rigids.add(poolelementstructurepiece);
                        }

                        for (JigsawJunction jigsawjunction : poolelementstructurepiece.getJunctions())
                        {
                            int k = jigsawjunction.getSourceX();
                            int l = jigsawjunction.getSourceZ();

                            if (k > i - 12 && l > j - 12 && k < i + 15 + 12 && l < j + 15 + 12)
                            {
                                this.junctions.add(jigsawjunction);
                            }
                        }
                    }
                    else
                    {
                        this.rigids.add(structurepiece);
                    }
                }
            }
        });
        this.pieceIterator = this.rigids.iterator();
        this.junctionIterator = this.junctions.iterator();
    }

    public double compute(DensityFunction.FunctionContext p_208200_)
    {
        int i = p_208200_.blockX();
        int j = p_208200_.blockY();
        int k = p_208200_.blockZ();
        double d0 = 0.0D;

        while (this.pieceIterator.hasNext())
        {
            StructurePiece structurepiece = this.pieceIterator.next();
            BoundingBox boundingbox = structurepiece.getBoundingBox();
            int l = Math.max(0, Math.max(boundingbox.minX() - i, i - boundingbox.maxX()));
            int i1 = j - (boundingbox.minY() + (structurepiece instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece)structurepiece).getGroundLevelDelta() : 0));
            int j1 = Math.max(0, Math.max(boundingbox.minZ() - k, k - boundingbox.maxZ()));
            NoiseEffect noiseeffect = structurepiece.getNoiseEffect();

            if (noiseeffect == NoiseEffect.BURY)
            {
                d0 += getBuryContribution(l, i1, j1);
            }
            else if (noiseeffect == NoiseEffect.BEARD)
            {
                d0 += getBeardContribution(l, i1, j1) * 0.8D;
            }
        }

        this.pieceIterator.back(this.rigids.size());

        while (this.junctionIterator.hasNext())
        {
            JigsawJunction jigsawjunction = this.junctionIterator.next();
            int k1 = i - jigsawjunction.getSourceX();
            int l1 = j - jigsawjunction.getSourceGroundY();
            int i2 = k - jigsawjunction.getSourceZ();
            d0 += getBeardContribution(k1, l1, i2) * 0.4D;
        }

        this.junctionIterator.back(this.junctions.size());
        return d0;
    }

    public double minValue()
    {
        return Double.NEGATIVE_INFINITY;
    }

    public double maxValue()
    {
        return Double.POSITIVE_INFINITY;
    }

    private static double getBuryContribution(int pX, int pY, int pZ)
    {
        double d0 = Mth.length((double)pX, (double)pY / 2.0D, (double)pZ);
        return Mth.clampedMap(d0, 0.0D, 6.0D, 1.0D, 0.0D);
    }

    private static double getBeardContribution(int pX, int pY, int pZ)
    {
        int i = pX + 12;
        int j = pY + 12;
        int k = pZ + 12;

        if (i >= 0 && i < 24)
        {
            if (j >= 0 && j < 24)
            {
                return k >= 0 && k < 24 ? (double)BEARD_KERNEL[k * 24 * 24 + i * 24 + j] : 0.0D;
            }
            else
            {
                return 0.0D;
            }
        }
        else
        {
            return 0.0D;
        }
    }

    private static double computeBeardContribution(int pX, int pY, int pZ)
    {
        double d0 = (double)(pX * pX + pZ * pZ);
        double d1 = (double)pY + 0.5D;
        double d2 = d1 * d1;
        double d3 = Math.pow(Math.E, -(d2 / 16.0D + d0 / 16.0D));
        double d4 = -d1 * Mth.fastInvSqrt(d2 / 2.0D + d0 / 2.0D) / 2.0D;
        return d4 * d3;
    }
}
