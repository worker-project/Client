package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.slf4j.Logger;

public class MineShaftPieces
{
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_SHAFT_WIDTH = 3;
    private static final int DEFAULT_SHAFT_HEIGHT = 3;
    private static final int DEFAULT_SHAFT_LENGTH = 5;
    private static final int MAX_PILLAR_HEIGHT = 20;
    private static final int MAX_CHAIN_HEIGHT = 50;
    private static final int MAX_DEPTH = 8;
    public static final int MAGIC_START_Y = 50;

    private static MineShaftPieces.MineShaftPiece createRandomShaftPiece(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, @Nullable Direction pDirection, int pGenDepth, MineshaftFeature.Type pType)
    {
        int i = pRandom.nextInt(100);

        if (i >= 80)
        {
            BoundingBox boundingbox = MineShaftPieces.MineShaftCrossing.findCrossing(pPieces, pRandom, pX, pY, pZ, pDirection);

            if (boundingbox != null)
            {
                return new MineShaftPieces.MineShaftCrossing(pGenDepth, boundingbox, pDirection, pType);
            }
        }
        else if (i >= 70)
        {
            BoundingBox boundingbox1 = MineShaftPieces.MineShaftStairs.findStairs(pPieces, pRandom, pX, pY, pZ, pDirection);

            if (boundingbox1 != null)
            {
                return new MineShaftPieces.MineShaftStairs(pGenDepth, boundingbox1, pDirection, pType);
            }
        }
        else
        {
            BoundingBox boundingbox2 = MineShaftPieces.MineShaftCorridor.findCorridorSize(pPieces, pRandom, pX, pY, pZ, pDirection);

            if (boundingbox2 != null)
            {
                return new MineShaftPieces.MineShaftCorridor(pGenDepth, pRandom, boundingbox2, pDirection, pType);
            }
        }

        return null;
    }

    static MineShaftPieces.MineShaftPiece generateAndAddPiece(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pDirection, int pGenDepth)
    {
        if (pGenDepth > 8)
        {
            return null;
        }
        else if (Math.abs(pX - pPiece.getBoundingBox().minX()) <= 80 && Math.abs(pZ - pPiece.getBoundingBox().minZ()) <= 80)
        {
            MineshaftFeature.Type mineshaftfeature$type = ((MineShaftPieces.MineShaftPiece)pPiece).type;
            MineShaftPieces.MineShaftPiece mineshaftpieces$mineshaftpiece = createRandomShaftPiece(pPieces, pRandom, pX, pY, pZ, pDirection, pGenDepth + 1, mineshaftfeature$type);

            if (mineshaftpieces$mineshaftpiece != null)
            {
                pPieces.addPiece(mineshaftpieces$mineshaftpiece);
                mineshaftpieces$mineshaftpiece.addChildren(pPiece, pPieces, pRandom);
            }

            return mineshaftpieces$mineshaftpiece;
        }
        else
        {
            return null;
        }
    }

    public static class MineShaftCorridor extends MineShaftPieces.MineShaftPiece
    {
        private final boolean hasRails;
        private final boolean spiderCorridor;
        private boolean hasPlacedSpider;
        private final int numSections;

        public MineShaftCorridor(CompoundTag p_192028_)
        {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, p_192028_);
            this.hasRails = p_192028_.getBoolean("hr");
            this.spiderCorridor = p_192028_.getBoolean("sc");
            this.hasPlacedSpider = p_192028_.getBoolean("hps");
            this.numSections = p_192028_.getInt("Num");
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192038_, CompoundTag p_192039_)
        {
            super.addAdditionalSaveData(p_192038_, p_192039_);
            p_192039_.putBoolean("hr", this.hasRails);
            p_192039_.putBoolean("sc", this.spiderCorridor);
            p_192039_.putBoolean("hps", this.hasPlacedSpider);
            p_192039_.putInt("Num", this.numSections);
        }

        public MineShaftCorridor(int pGenDepth, Random pRandom, BoundingBox pBox, Direction pDirection, MineshaftFeature.Type pType)
        {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, pGenDepth, pType, pBox);
            this.setOrientation(pDirection);
            this.hasRails = pRandom.nextInt(3) == 0;
            this.spiderCorridor = !this.hasRails && pRandom.nextInt(23) == 0;

            if (this.getOrientation().getAxis() == Direction.Axis.Z)
            {
                this.numSections = pBox.getZSpan() / 5;
            }
            else
            {
                this.numSections = pBox.getXSpan() / 5;
            }
        }

        @Nullable
        public static BoundingBox findCorridorSize(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pDirection)
        {
            for (int i = pRandom.nextInt(3) + 2; i > 0; --i)
            {
                int j = i * 5;
                BoundingBox boundingbox;

                switch (pDirection)
                {
                    case NORTH:
                    default:
                        boundingbox = new BoundingBox(0, 0, -(j - 1), 2, 2, 0);
                        break;

                    case SOUTH:
                        boundingbox = new BoundingBox(0, 0, 0, 2, 2, j - 1);
                        break;

                    case WEST:
                        boundingbox = new BoundingBox(-(j - 1), 0, 0, 0, 2, 2);
                        break;

                    case EAST:
                        boundingbox = new BoundingBox(0, 0, 0, j - 1, 2, 2);
                }

                boundingbox.move(pX, pY, pZ);

                if (pPieces.findCollisionPiece(boundingbox) == null)
                {
                    return boundingbox;
                }
            }

            return null;
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            int i = this.getGenDepth();
            int j = pRandom.nextInt(4);
            Direction direction = this.getOrientation();

            if (direction != null)
            {
                switch (direction)
                {
                    case NORTH:
                    default:
                        if (j <= 1)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX(), this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.minZ() - 1, direction, i);
                        }
                        else if (j == 2)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.minZ(), Direction.WEST, i);
                        }
                        else
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.minZ(), Direction.EAST, i);
                        }

                        break;

                    case SOUTH:
                        if (j <= 1)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX(), this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.maxZ() + 1, direction, i);
                        }
                        else if (j == 2)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.maxZ() - 3, Direction.WEST, i);
                        }
                        else
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.maxZ() - 3, Direction.EAST, i);
                        }

                        break;

                    case WEST:
                        if (j <= 1)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.minZ(), direction, i);
                        }
                        else if (j == 2)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX(), this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                        }
                        else
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX(), this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                        }

                        break;

                    case EAST:
                        if (j <= 1)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.minZ(), direction, i);
                        }
                        else if (j == 2)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() - 3, this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                        }
                        else
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() - 3, this.boundingBox.minY() - 1 + pRandom.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                        }
                }
            }

            if (i < 8)
            {
                if (direction != Direction.NORTH && direction != Direction.SOUTH)
                {
                    for (int i1 = this.boundingBox.minX() + 3; i1 + 3 <= this.boundingBox.maxX(); i1 += 5)
                    {
                        int j1 = pRandom.nextInt(5);

                        if (j1 == 0)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, i1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i + 1);
                        }
                        else if (j1 == 1)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, i1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i + 1);
                        }
                    }
                }
                else
                {
                    for (int k = this.boundingBox.minZ() + 3; k + 3 <= this.boundingBox.maxZ(); k += 5)
                    {
                        int l = pRandom.nextInt(5);

                        if (l == 0)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY(), k, Direction.WEST, i + 1);
                        }
                        else if (l == 1)
                        {
                            MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY(), k, Direction.EAST, i + 1);
                        }
                    }
                }
            }
        }

        protected boolean createChest(WorldGenLevel pLevel, BoundingBox pBox, Random pRandom, int pX, int pY, int pZ, ResourceLocation pLoot)
        {
            BlockPos blockpos = this.getWorldPos(pX, pY, pZ);

            if (pBox.isInside(blockpos) && pLevel.getBlockState(blockpos).isAir() && !pLevel.getBlockState(blockpos.below()).isAir())
            {
                BlockState blockstate = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, pRandom.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
                this.placeBlock(pLevel, blockstate, pX, pY, pZ, pBox);
                MinecartChest minecartchest = new MinecartChest(pLevel.getLevel(), (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D);
                minecartchest.setLootTable(pLoot, pRandom.nextLong());
                pLevel.addFreshEntity(minecartchest);
                return true;
            }
            else
            {
                return false;
            }
        }

        public void postProcess(WorldGenLevel p_192030_, StructureFeatureManager p_192031_, ChunkGenerator p_192032_, Random p_192033_, BoundingBox p_192034_, ChunkPos p_192035_, BlockPos p_192036_)
        {
            if (!this.edgesLiquid(p_192030_, p_192034_))
            {
                int i = 0;
                int j = 2;
                int k = 0;
                int l = 2;
                int i1 = this.numSections * 5 - 1;
                BlockState blockstate = this.type.getPlanksState();
                this.generateBox(p_192030_, p_192034_, 0, 0, 0, 2, 1, i1, CAVE_AIR, CAVE_AIR, false);
                this.generateMaybeBox(p_192030_, p_192034_, p_192033_, 0.8F, 0, 2, 0, 2, 2, i1, CAVE_AIR, CAVE_AIR, false, false);

                if (this.spiderCorridor)
                {
                    this.generateMaybeBox(p_192030_, p_192034_, p_192033_, 0.6F, 0, 0, 0, 2, 1, i1, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
                }

                for (int j1 = 0; j1 < this.numSections; ++j1)
                {
                    int k1 = 2 + j1 * 5;
                    this.placeSupport(p_192030_, p_192034_, 0, 0, k1, 2, 2, p_192033_);
                    this.maybePlaceCobWeb(p_192030_, p_192034_, p_192033_, 0.1F, 0, 2, k1 - 1);
                    this.maybePlaceCobWeb(p_192030_, p_192034_, p_192033_, 0.1F, 2, 2, k1 - 1);
                    this.maybePlaceCobWeb(p_192030_, p_192034_, p_192033_, 0.1F, 0, 2, k1 + 1);
                    this.maybePlaceCobWeb(p_192030_, p_192034_, p_192033_, 0.1F, 2, 2, k1 + 1);
                    this.maybePlaceCobWeb(p_192030_, p_192034_, p_192033_, 0.05F, 0, 2, k1 - 2);
                    this.maybePlaceCobWeb(p_192030_, p_192034_, p_192033_, 0.05F, 2, 2, k1 - 2);
                    this.maybePlaceCobWeb(p_192030_, p_192034_, p_192033_, 0.05F, 0, 2, k1 + 2);
                    this.maybePlaceCobWeb(p_192030_, p_192034_, p_192033_, 0.05F, 2, 2, k1 + 2);

                    if (p_192033_.nextInt(100) == 0)
                    {
                        this.createChest(p_192030_, p_192034_, p_192033_, 2, 0, k1 - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                    }

                    if (p_192033_.nextInt(100) == 0)
                    {
                        this.createChest(p_192030_, p_192034_, p_192033_, 0, 0, k1 + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                    }

                    if (this.spiderCorridor && !this.hasPlacedSpider)
                    {
                        int l1 = 1;
                        int i2 = k1 - 1 + p_192033_.nextInt(3);
                        BlockPos blockpos = this.getWorldPos(1, 0, i2);

                        if (p_192034_.isInside(blockpos) && this.isInterior(p_192030_, 1, 0, i2, p_192034_))
                        {
                            this.hasPlacedSpider = true;
                            p_192030_.setBlock(blockpos, Blocks.SPAWNER.defaultBlockState(), 2);
                            BlockEntity blockentity = p_192030_.getBlockEntity(blockpos);

                            if (blockentity instanceof SpawnerBlockEntity)
                            {
                                ((SpawnerBlockEntity)blockentity).getSpawner().setEntityId(EntityType.CAVE_SPIDER);
                            }
                        }
                    }
                }

                for (int j2 = 0; j2 <= 2; ++j2)
                {
                    for (int l2 = 0; l2 <= i1; ++l2)
                    {
                        this.setPlanksBlock(p_192030_, p_192034_, blockstate, j2, -1, l2);
                    }
                }

                int k2 = 2;
                this.placeDoubleLowerOrUpperSupport(p_192030_, p_192034_, 0, -1, 2);

                if (this.numSections > 1)
                {
                    int i3 = i1 - 2;
                    this.placeDoubleLowerOrUpperSupport(p_192030_, p_192034_, 0, -1, i3);
                }

                if (this.hasRails)
                {
                    BlockState blockstate1 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

                    for (int j3 = 0; j3 <= i1; ++j3)
                    {
                        BlockState blockstate2 = this.getBlock(p_192030_, 1, -1, j3, p_192034_);

                        if (!blockstate2.isAir() && blockstate2.isSolidRender(p_192030_, this.getWorldPos(1, -1, j3)))
                        {
                            float f = this.isInterior(p_192030_, 1, 0, j3, p_192034_) ? 0.7F : 0.9F;
                            this.maybeGenerateBlock(p_192030_, p_192034_, p_192033_, f, 1, 0, j3, blockstate1);
                        }
                    }
                }
            }
        }

        private void placeDoubleLowerOrUpperSupport(WorldGenLevel pLevel, BoundingBox pBox, int pX, int pY, int pZ)
        {
            BlockState blockstate = this.type.getWoodState();
            BlockState blockstate1 = this.type.getPlanksState();

            if (this.getBlock(pLevel, pX, pY, pZ, pBox).is(blockstate1.getBlock()))
            {
                this.fillPillarDownOrChainUp(pLevel, blockstate, pX, pY, pZ, pBox);
            }

            if (this.getBlock(pLevel, pX + 2, pY, pZ, pBox).is(blockstate1.getBlock()))
            {
                this.fillPillarDownOrChainUp(pLevel, blockstate, pX + 2, pY, pZ, pBox);
            }
        }

        protected void fillColumnDown(WorldGenLevel pLevel, BlockState pState, int pX, int pY, int pZ, BoundingBox pBox)
        {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = this.getWorldPos(pX, pY, pZ);

            if (pBox.isInside(blockpos$mutableblockpos))
            {
                int i = blockpos$mutableblockpos.getY();

                while (this.isReplaceableByStructures(pLevel.getBlockState(blockpos$mutableblockpos)) && blockpos$mutableblockpos.getY() > pLevel.getMinBuildHeight() + 1)
                {
                    blockpos$mutableblockpos.move(Direction.DOWN);
                }

                if (this.canPlaceColumnOnTopOf(pLevel, blockpos$mutableblockpos, pLevel.getBlockState(blockpos$mutableblockpos)))
                {
                    while (blockpos$mutableblockpos.getY() < i)
                    {
                        blockpos$mutableblockpos.move(Direction.UP);
                        pLevel.setBlock(blockpos$mutableblockpos, pState, 2);
                    }
                }
            }
        }

        protected void fillPillarDownOrChainUp(WorldGenLevel pLevel, BlockState pState, int pX, int pY, int pZ, BoundingBox pBox)
        {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = this.getWorldPos(pX, pY, pZ);

            if (pBox.isInside(blockpos$mutableblockpos))
            {
                int i = blockpos$mutableblockpos.getY();
                int j = 1;
                boolean flag = true;

                for (boolean flag1 = true; flag || flag1; ++j)
                {
                    if (flag)
                    {
                        blockpos$mutableblockpos.setY(i - j);
                        BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
                        boolean flag2 = this.isReplaceableByStructures(blockstate) && !blockstate.is(Blocks.LAVA);

                        if (!flag2 && this.canPlaceColumnOnTopOf(pLevel, blockpos$mutableblockpos, blockstate))
                        {
                            fillColumnBetween(pLevel, pState, blockpos$mutableblockpos, i - j + 1, i);
                            return;
                        }

                        flag = j <= 20 && flag2 && blockpos$mutableblockpos.getY() > pLevel.getMinBuildHeight() + 1;
                    }

                    if (flag1)
                    {
                        blockpos$mutableblockpos.setY(i + j);
                        BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos);
                        boolean flag3 = this.isReplaceableByStructures(blockstate1);

                        if (!flag3 && this.canHangChainBelow(pLevel, blockpos$mutableblockpos, blockstate1))
                        {
                            pLevel.setBlock(blockpos$mutableblockpos.setY(i + 1), this.type.getFenceState(), 2);
                            fillColumnBetween(pLevel, Blocks.CHAIN.defaultBlockState(), blockpos$mutableblockpos, i + 2, i + j);
                            return;
                        }

                        flag1 = j <= 50 && flag3 && blockpos$mutableblockpos.getY() < pLevel.getMaxBuildHeight() - 1;
                    }
                }
            }
        }

        private static void fillColumnBetween(WorldGenLevel pLevel, BlockState pState, BlockPos.MutableBlockPos pPos, int pMinY, int pMaxY)
        {
            for (int i = pMinY; i < pMaxY; ++i)
            {
                pLevel.setBlock(pPos.setY(i), pState, 2);
            }
        }

        private boolean canPlaceColumnOnTopOf(LevelReader p_203133_, BlockPos p_203134_, BlockState p_203135_)
        {
            return p_203135_.isFaceSturdy(p_203133_, p_203134_, Direction.UP);
        }

        private boolean canHangChainBelow(LevelReader pLevel, BlockPos pPos, BlockState pState)
        {
            return Block.canSupportCenter(pLevel, pPos, Direction.DOWN) && !(pState.getBlock() instanceof FallingBlock);
        }

        private void placeSupport(WorldGenLevel pLevel, BoundingBox pBox, int pMinX, int pMinY, int pZ, int pMaxY, int pMaxX, Random pRandom)
        {
            if (this.isSupportingBox(pLevel, pBox, pMinX, pMaxX, pMaxY, pZ))
            {
                BlockState blockstate = this.type.getPlanksState();
                BlockState blockstate1 = this.type.getFenceState();
                this.generateBox(pLevel, pBox, pMinX, pMinY, pZ, pMinX, pMaxY - 1, pZ, blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), CAVE_AIR, false);
                this.generateBox(pLevel, pBox, pMaxX, pMinY, pZ, pMaxX, pMaxY - 1, pZ, blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), CAVE_AIR, false);

                if (pRandom.nextInt(4) == 0)
                {
                    this.generateBox(pLevel, pBox, pMinX, pMaxY, pZ, pMinX, pMaxY, pZ, blockstate, CAVE_AIR, false);
                    this.generateBox(pLevel, pBox, pMaxX, pMaxY, pZ, pMaxX, pMaxY, pZ, blockstate, CAVE_AIR, false);
                }
                else
                {
                    this.generateBox(pLevel, pBox, pMinX, pMaxY, pZ, pMaxX, pMaxY, pZ, blockstate, CAVE_AIR, false);
                    this.maybeGenerateBlock(pLevel, pBox, pRandom, 0.05F, pMinX + 1, pMaxY, pZ - 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH));
                    this.maybeGenerateBlock(pLevel, pBox, pRandom, 0.05F, pMinX + 1, pMaxY, pZ + 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH));
                }
            }
        }

        private void maybePlaceCobWeb(WorldGenLevel pLevel, BoundingBox pBox, Random pRandom, float pChance, int pX, int pY, int pZ)
        {
            if (this.isInterior(pLevel, pX, pY, pZ, pBox) && pRandom.nextFloat() < pChance && this.hasSturdyNeighbours(pLevel, pBox, pX, pY, pZ, 2))
            {
                this.placeBlock(pLevel, Blocks.COBWEB.defaultBlockState(), pX, pY, pZ, pBox);
            }
        }

        private boolean hasSturdyNeighbours(WorldGenLevel pLevel, BoundingBox pBox, int pX, int pY, int pZ, int pRequired)
        {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = this.getWorldPos(pX, pY, pZ);
            int i = 0;

            for (Direction direction : Direction.values())
            {
                blockpos$mutableblockpos.move(direction);

                if (pBox.isInside(blockpos$mutableblockpos) && pLevel.getBlockState(blockpos$mutableblockpos).isFaceSturdy(pLevel, blockpos$mutableblockpos, direction.getOpposite()))
                {
                    ++i;

                    if (i >= pRequired)
                    {
                        return true;
                    }
                }

                blockpos$mutableblockpos.move(direction.getOpposite());
            }

            return false;
        }
    }

    public static class MineShaftCrossing extends MineShaftPieces.MineShaftPiece
    {
        private final Direction direction;
        private final boolean isTwoFloored;

        public MineShaftCrossing(CompoundTag p_192041_)
        {
            super(StructurePieceType.MINE_SHAFT_CROSSING, p_192041_);
            this.isTwoFloored = p_192041_.getBoolean("tf");
            this.direction = Direction.from2DDataValue(p_192041_.getInt("D"));
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192051_, CompoundTag p_192052_)
        {
            super.addAdditionalSaveData(p_192051_, p_192052_);
            p_192052_.putBoolean("tf", this.isTwoFloored);
            p_192052_.putInt("D", this.direction.get2DDataValue());
        }

        public MineShaftCrossing(int pGenDepth, BoundingBox pBox, @Nullable Direction pDirection, MineshaftFeature.Type pType)
        {
            super(StructurePieceType.MINE_SHAFT_CROSSING, pGenDepth, pType, pBox);
            this.direction = pDirection;
            this.isTwoFloored = pBox.getYSpan() > 3;
        }

        @Nullable
        public static BoundingBox findCrossing(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pDirection)
        {
            int i;

            if (pRandom.nextInt(4) == 0)
            {
                i = 6;
            }
            else
            {
                i = 2;
            }

            BoundingBox boundingbox;

            switch (pDirection)
            {
                case NORTH:
                default:
                    boundingbox = new BoundingBox(-1, 0, -4, 3, i, 0);
                    break;

                case SOUTH:
                    boundingbox = new BoundingBox(-1, 0, 0, 3, i, 4);
                    break;

                case WEST:
                    boundingbox = new BoundingBox(-4, 0, -1, 0, i, 3);
                    break;

                case EAST:
                    boundingbox = new BoundingBox(0, 0, -1, 4, i, 3);
            }

            boundingbox.move(pX, pY, pZ);
            return pPieces.findCollisionPiece(boundingbox) != null ? null : boundingbox;
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            int i = this.getGenDepth();

            switch (this.direction)
            {
                case NORTH:
                default:
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, i);
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, i);
                    break;

                case SOUTH:
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, i);
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, i);
                    break;

                case WEST:
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, i);
                    break;

                case EAST:
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, i);
            }

            if (this.isTwoFloored)
            {
                if (pRandom.nextBoolean())
                {
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() - 1, Direction.NORTH, i);
                }

                if (pRandom.nextBoolean())
                {
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.WEST, i);
                }

                if (pRandom.nextBoolean())
                {
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.EAST, i);
                }

                if (pRandom.nextBoolean())
                {
                    MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                }
            }
        }

        public void postProcess(WorldGenLevel p_192043_, StructureFeatureManager p_192044_, ChunkGenerator p_192045_, Random p_192046_, BoundingBox p_192047_, ChunkPos p_192048_, BlockPos p_192049_)
        {
            if (!this.edgesLiquid(p_192043_, p_192047_))
            {
                BlockState blockstate = this.type.getPlanksState();

                if (this.isTwoFloored)
                {
                    this.generateBox(p_192043_, p_192047_, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.minY() + 3 - 1, this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                    this.generateBox(p_192043_, p_192047_, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.minY() + 3 - 1, this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
                    this.generateBox(p_192043_, p_192047_, this.boundingBox.minX() + 1, this.boundingBox.maxY() - 2, this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                    this.generateBox(p_192043_, p_192047_, this.boundingBox.minX(), this.boundingBox.maxY() - 2, this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
                    this.generateBox(p_192043_, p_192047_, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3, this.boundingBox.minZ() + 1, this.boundingBox.maxX() - 1, this.boundingBox.minY() + 3, this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
                }
                else
                {
                    this.generateBox(p_192043_, p_192047_, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                    this.generateBox(p_192043_, p_192047_, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
                }

                this.placeSupportPillar(p_192043_, p_192047_, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
                this.placeSupportPillar(p_192043_, p_192047_, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
                this.placeSupportPillar(p_192043_, p_192047_, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
                this.placeSupportPillar(p_192043_, p_192047_, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
                int i = this.boundingBox.minY() - 1;

                for (int j = this.boundingBox.minX(); j <= this.boundingBox.maxX(); ++j)
                {
                    for (int k = this.boundingBox.minZ(); k <= this.boundingBox.maxZ(); ++k)
                    {
                        this.setPlanksBlock(p_192043_, p_192047_, blockstate, j, i, k);
                    }
                }
            }
        }

        private void placeSupportPillar(WorldGenLevel pLevel, BoundingBox pBox, int pX, int pY, int pZ, int pMaxY)
        {
            if (!this.getBlock(pLevel, pX, pMaxY + 1, pZ, pBox).isAir())
            {
                this.generateBox(pLevel, pBox, pX, pY, pZ, pX, pMaxY, pZ, this.type.getPlanksState(), CAVE_AIR, false);
            }
        }
    }

    abstract static class MineShaftPiece extends StructurePiece
    {
        protected MineshaftFeature.Type type;

        public MineShaftPiece(StructurePieceType pPieceType, int pGenDepth, MineshaftFeature.Type pType, BoundingBox pBox)
        {
            super(pPieceType, pGenDepth, pBox);
            this.type = pType;
        }

        public MineShaftPiece(StructurePieceType p_209881_, CompoundTag p_209882_)
        {
            super(p_209881_, p_209882_);
            this.type = MineshaftFeature.Type.byId(p_209882_.getInt("MST"));
        }

        protected boolean canBeReplaced(LevelReader pLevel, int pX, int pY, int pZ, BoundingBox pBox)
        {
            BlockState blockstate = this.getBlock(pLevel, pX, pY, pZ, pBox);
            return !blockstate.is(this.type.getPlanksState().getBlock()) && !blockstate.is(this.type.getWoodState().getBlock()) && !blockstate.is(this.type.getFenceState().getBlock()) && !blockstate.is(Blocks.CHAIN);
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192054_, CompoundTag p_192055_)
        {
            p_192055_.putInt("MST", this.type.ordinal());
        }

        protected boolean isSupportingBox(BlockGetter pLevel, BoundingBox pBox, int pXStart, int pXEnd, int pY, int pZ)
        {
            for (int i = pXStart; i <= pXEnd; ++i)
            {
                if (this.getBlock(pLevel, i, pY + 1, pZ, pBox).isAir())
                {
                    return false;
                }
            }

            return true;
        }

        protected boolean edgesLiquid(BlockGetter pLevel, BoundingBox pBox)
        {
            int i = Math.max(this.boundingBox.minX() - 1, pBox.minX());
            int j = Math.max(this.boundingBox.minY() - 1, pBox.minY());
            int k = Math.max(this.boundingBox.minZ() - 1, pBox.minZ());
            int l = Math.min(this.boundingBox.maxX() + 1, pBox.maxX());
            int i1 = Math.min(this.boundingBox.maxY() + 1, pBox.maxY());
            int j1 = Math.min(this.boundingBox.maxZ() + 1, pBox.maxZ());
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k1 = i; k1 <= l; ++k1)
            {
                for (int l1 = k; l1 <= j1; ++l1)
                {
                    if (pLevel.getBlockState(blockpos$mutableblockpos.set(k1, j, l1)).getMaterial().isLiquid())
                    {
                        return true;
                    }

                    if (pLevel.getBlockState(blockpos$mutableblockpos.set(k1, i1, l1)).getMaterial().isLiquid())
                    {
                        return true;
                    }
                }
            }

            for (int i2 = i; i2 <= l; ++i2)
            {
                for (int k2 = j; k2 <= i1; ++k2)
                {
                    if (pLevel.getBlockState(blockpos$mutableblockpos.set(i2, k2, k)).getMaterial().isLiquid())
                    {
                        return true;
                    }

                    if (pLevel.getBlockState(blockpos$mutableblockpos.set(i2, k2, j1)).getMaterial().isLiquid())
                    {
                        return true;
                    }
                }
            }

            for (int j2 = k; j2 <= j1; ++j2)
            {
                for (int l2 = j; l2 <= i1; ++l2)
                {
                    if (pLevel.getBlockState(blockpos$mutableblockpos.set(i, l2, j2)).getMaterial().isLiquid())
                    {
                        return true;
                    }

                    if (pLevel.getBlockState(blockpos$mutableblockpos.set(l, l2, j2)).getMaterial().isLiquid())
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        protected void setPlanksBlock(WorldGenLevel pLevel, BoundingBox pBox, BlockState pPlankState, int pX, int pY, int pZ)
        {
            if (this.isInterior(pLevel, pX, pY, pZ, pBox))
            {
                BlockPos blockpos = this.getWorldPos(pX, pY, pZ);
                BlockState blockstate = pLevel.getBlockState(blockpos);

                if (!blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP))
                {
                    pLevel.setBlock(blockpos, pPlankState, 2);
                }
            }
        }
    }

    public static class MineShaftRoom extends MineShaftPieces.MineShaftPiece
    {
        private final List<BoundingBox> childEntranceBoxes = Lists.newLinkedList();

        public MineShaftRoom(int pGenDepth, Random pRandom, int pX, int pZ, MineshaftFeature.Type pType)
        {
            super(StructurePieceType.MINE_SHAFT_ROOM, pGenDepth, pType, new BoundingBox(pX, 50, pZ, pX + 7 + pRandom.nextInt(6), 54 + pRandom.nextInt(6), pZ + 7 + pRandom.nextInt(6)));
            this.type = pType;
        }

        public MineShaftRoom(CompoundTag p_192057_)
        {
            super(StructurePieceType.MINE_SHAFT_ROOM, p_192057_);
            BoundingBox.CODEC.listOf().parse(NbtOps.INSTANCE, p_192057_.getList("Entrances", 11)).resultOrPartial(MineShaftPieces.LOGGER::error).ifPresent(this.childEntranceBoxes::addAll);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            int i = this.getGenDepth();
            int j = this.boundingBox.getYSpan() - 3 - 1;

            if (j <= 0)
            {
                j = 1;
            }

            int k;

            for (k = 0; k < this.boundingBox.getXSpan(); k += 4)
            {
                k += pRandom.nextInt(this.boundingBox.getXSpan());

                if (k + 3 > this.boundingBox.getXSpan())
                {
                    break;
                }

                MineShaftPieces.MineShaftPiece mineshaftpieces$mineshaftpiece = MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + k, this.boundingBox.minY() + pRandom.nextInt(j) + 1, this.boundingBox.minZ() - 1, Direction.NORTH, i);

                if (mineshaftpieces$mineshaftpiece != null)
                {
                    BoundingBox boundingbox = mineshaftpieces$mineshaftpiece.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(boundingbox.minX(), boundingbox.minY(), this.boundingBox.minZ(), boundingbox.maxX(), boundingbox.maxY(), this.boundingBox.minZ() + 1));
                }
            }

            for (k = 0; k < this.boundingBox.getXSpan(); k += 4)
            {
                k += pRandom.nextInt(this.boundingBox.getXSpan());

                if (k + 3 > this.boundingBox.getXSpan())
                {
                    break;
                }

                MineShaftPieces.MineShaftPiece mineshaftpieces$mineshaftpiece1 = MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() + k, this.boundingBox.minY() + pRandom.nextInt(j) + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, i);

                if (mineshaftpieces$mineshaftpiece1 != null)
                {
                    BoundingBox boundingbox1 = mineshaftpieces$mineshaftpiece1.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(boundingbox1.minX(), boundingbox1.minY(), this.boundingBox.maxZ() - 1, boundingbox1.maxX(), boundingbox1.maxY(), this.boundingBox.maxZ()));
                }
            }

            for (k = 0; k < this.boundingBox.getZSpan(); k += 4)
            {
                k += pRandom.nextInt(this.boundingBox.getZSpan());

                if (k + 3 > this.boundingBox.getZSpan())
                {
                    break;
                }

                MineShaftPieces.MineShaftPiece mineshaftpieces$mineshaftpiece2 = MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() + pRandom.nextInt(j) + 1, this.boundingBox.minZ() + k, Direction.WEST, i);

                if (mineshaftpieces$mineshaftpiece2 != null)
                {
                    BoundingBox boundingbox2 = mineshaftpieces$mineshaftpiece2.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.minX(), boundingbox2.minY(), boundingbox2.minZ(), this.boundingBox.minX() + 1, boundingbox2.maxY(), boundingbox2.maxZ()));
                }
            }

            for (k = 0; k < this.boundingBox.getZSpan(); k += 4)
            {
                k += pRandom.nextInt(this.boundingBox.getZSpan());

                if (k + 3 > this.boundingBox.getZSpan())
                {
                    break;
                }

                StructurePiece structurepiece = MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() + pRandom.nextInt(j) + 1, this.boundingBox.minZ() + k, Direction.EAST, i);

                if (structurepiece != null)
                {
                    BoundingBox boundingbox3 = structurepiece.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.maxX() - 1, boundingbox3.minY(), boundingbox3.minZ(), this.boundingBox.maxX(), boundingbox3.maxY(), boundingbox3.maxZ()));
                }
            }
        }

        public void postProcess(WorldGenLevel p_192059_, StructureFeatureManager p_192060_, ChunkGenerator p_192061_, Random p_192062_, BoundingBox p_192063_, ChunkPos p_192064_, BlockPos p_192065_)
        {
            if (!this.edgesLiquid(p_192059_, p_192063_))
            {
                this.generateBox(p_192059_, p_192063_, this.boundingBox.minX(), this.boundingBox.minY() + 1, this.boundingBox.minZ(), this.boundingBox.maxX(), Math.min(this.boundingBox.minY() + 3, this.boundingBox.maxY()), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);

                for (BoundingBox boundingbox : this.childEntranceBoxes)
                {
                    this.generateBox(p_192059_, p_192063_, boundingbox.minX(), boundingbox.maxY() - 2, boundingbox.minZ(), boundingbox.maxX(), boundingbox.maxY(), boundingbox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                }

                this.generateUpperHalfSphere(p_192059_, p_192063_, this.boundingBox.minX(), this.boundingBox.minY() + 4, this.boundingBox.minZ(), this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, false);
            }
        }

        public void move(int pX, int pY, int pZ)
        {
            super.move(pX, pY, pZ);

            for (BoundingBox boundingbox : this.childEntranceBoxes)
            {
                boundingbox.move(pX, pY, pZ);
            }
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192067_, CompoundTag p_192068_)
        {
            super.addAdditionalSaveData(p_192067_, p_192068_);
            BoundingBox.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.childEntranceBoxes).resultOrPartial(MineShaftPieces.LOGGER::error).ifPresent((p_162606_) ->
            {
                p_192068_.put("Entrances", p_162606_);
            });
        }
    }

    public static class MineShaftStairs extends MineShaftPieces.MineShaftPiece
    {
        public MineShaftStairs(int pGenDepth, BoundingBox pBox, Direction pDirection, MineshaftFeature.Type pType)
        {
            super(StructurePieceType.MINE_SHAFT_STAIRS, pGenDepth, pType, pBox);
            this.setOrientation(pDirection);
        }

        public MineShaftStairs(CompoundTag p_192070_)
        {
            super(StructurePieceType.MINE_SHAFT_STAIRS, p_192070_);
        }

        @Nullable
        public static BoundingBox findStairs(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pDirection)
        {
            BoundingBox boundingbox;

            switch (pDirection)
            {
                case NORTH:
                default:
                    boundingbox = new BoundingBox(0, -5, -8, 2, 2, 0);
                    break;

                case SOUTH:
                    boundingbox = new BoundingBox(0, -5, 0, 2, 2, 8);
                    break;

                case WEST:
                    boundingbox = new BoundingBox(-8, -5, 0, 0, 2, 2);
                    break;

                case EAST:
                    boundingbox = new BoundingBox(0, -5, 0, 8, 2, 2);
            }

            boundingbox.move(pX, pY, pZ);
            return pPieces.findCollisionPiece(boundingbox) != null ? null : boundingbox;
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            int i = this.getGenDepth();
            Direction direction = this.getOrientation();

            if (direction != null)
            {
                switch (direction)
                {
                    case NORTH:
                    default:
                        MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                        break;

                    case SOUTH:
                        MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                        break;

                    case WEST:
                        MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.WEST, i);
                        break;

                    case EAST:
                        MineShaftPieces.generateAndAddPiece(pPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.EAST, i);
                }
            }
        }

        public void postProcess(WorldGenLevel p_192072_, StructureFeatureManager p_192073_, ChunkGenerator p_192074_, Random p_192075_, BoundingBox p_192076_, ChunkPos p_192077_, BlockPos p_192078_)
        {
            if (!this.edgesLiquid(p_192072_, p_192076_))
            {
                this.generateBox(p_192072_, p_192076_, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
                this.generateBox(p_192072_, p_192076_, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);

                for (int i = 0; i < 5; ++i)
                {
                    this.generateBox(p_192072_, p_192076_, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, CAVE_AIR, CAVE_AIR, false);
                }
            }
        }
    }
}
