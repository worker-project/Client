package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class NetherBridgePieces
{
    private static final int MAX_DEPTH = 30;
    private static final int LOWEST_Y_POSITION = 10;
    public static final int MAGIC_START_Y = 64;
    static final NetherBridgePieces.PieceWeight[] BRIDGE_PIECE_WEIGHTS = new NetherBridgePieces.PieceWeight[] {new NetherBridgePieces.PieceWeight(NetherBridgePieces.BridgeStraight.class, 30, 0, true), new NetherBridgePieces.PieceWeight(NetherBridgePieces.BridgeCrossing.class, 10, 4), new NetherBridgePieces.PieceWeight(NetherBridgePieces.RoomCrossing.class, 10, 4), new NetherBridgePieces.PieceWeight(NetherBridgePieces.StairsRoom.class, 10, 3), new NetherBridgePieces.PieceWeight(NetherBridgePieces.MonsterThrone.class, 5, 2), new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleEntrance.class, 5, 1)};
    static final NetherBridgePieces.PieceWeight[] CASTLE_PIECE_WEIGHTS = new NetherBridgePieces.PieceWeight[] {new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleSmallCorridorPiece.class, 25, 0, true), new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleSmallCorridorCrossingPiece.class, 15, 5), new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleSmallCorridorRightTurnPiece.class, 5, 10), new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleSmallCorridorLeftTurnPiece.class, 5, 10), new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleCorridorStairsPiece.class, 10, 3, true), new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleCorridorTBalconyPiece.class, 7, 2), new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleStalkRoom.class, 5, 2)};

    static NetherBridgePieces.NetherBridgePiece findAndCreateBridgePieceFactory(NetherBridgePieces.PieceWeight pWeight, StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
    {
        Class <? extends NetherBridgePieces.NetherBridgePiece > oclass = pWeight.pieceClass;
        NetherBridgePieces.NetherBridgePiece netherbridgepieces$netherbridgepiece = null;

        if (oclass == NetherBridgePieces.BridgeStraight.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.BridgeStraight.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.BridgeCrossing.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.BridgeCrossing.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.RoomCrossing.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.RoomCrossing.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.StairsRoom.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.StairsRoom.createPiece(pPieces, pX, pY, pZ, pGenDepth, pOrientation);
        }
        else if (oclass == NetherBridgePieces.MonsterThrone.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.MonsterThrone.createPiece(pPieces, pX, pY, pZ, pGenDepth, pOrientation);
        }
        else if (oclass == NetherBridgePieces.CastleEntrance.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.CastleEntrance.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.CastleSmallCorridorPiece.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.CastleSmallCorridorPiece.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.CastleSmallCorridorRightTurnPiece.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.CastleSmallCorridorRightTurnPiece.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.CastleSmallCorridorLeftTurnPiece.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.CastleSmallCorridorLeftTurnPiece.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.CastleCorridorStairsPiece.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.CastleCorridorStairsPiece.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.CastleCorridorTBalconyPiece.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.CastleCorridorTBalconyPiece.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.CastleSmallCorridorCrossingPiece.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.CastleSmallCorridorCrossingPiece.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
        }
        else if (oclass == NetherBridgePieces.CastleStalkRoom.class)
        {
            netherbridgepieces$netherbridgepiece = NetherBridgePieces.CastleStalkRoom.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
        }

        return netherbridgepieces$netherbridgepiece;
    }

    public static class BridgeCrossing extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 19;
        private static final int HEIGHT = 10;
        private static final int DEPTH = 19;

        public BridgeCrossing(int pX, BoundingBox pZ, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, pX, pZ);
            this.setOrientation(pOrientation);
        }

        protected BridgeCrossing(int pX, int pZ, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0, StructurePiece.makeBoundingBox(pX, 64, pZ, pOrientation, 19, 10, 19));
            this.setOrientation(pOrientation);
        }

        protected BridgeCrossing(StructurePieceType p_209884_, CompoundTag p_209885_)
        {
            super(p_209884_, p_209885_);
        }

        public BridgeCrossing(CompoundTag p_192081_)
        {
            this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, p_192081_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 8, 3, false);
            this.generateChildLeft((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 3, 8, false);
            this.generateChildRight((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 3, 8, false);
        }

        public static NetherBridgePieces.BridgeCrossing createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -8, -3, 0, 19, 10, 19, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.BridgeCrossing(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192083_, StructureFeatureManager p_192084_, ChunkGenerator p_192085_, Random p_192086_, BoundingBox p_192087_, ChunkPos p_192088_, BlockPos p_192089_)
        {
            this.generateBox(p_192083_, p_192087_, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 8, 5, 0, 10, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 0, 5, 8, 18, 7, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int i = 7; i <= 11; ++i)
            {
                for (int j = 0; j <= 2; ++j)
                {
                    this.fillColumnDown(p_192083_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192087_);
                    this.fillColumnDown(p_192083_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - j, p_192087_);
                }
            }

            this.generateBox(p_192083_, p_192087_, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192083_, p_192087_, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int k = 0; k <= 2; ++k)
            {
                for (int l = 7; l <= 11; ++l)
                {
                    this.fillColumnDown(p_192083_, Blocks.NETHER_BRICKS.defaultBlockState(), k, -1, l, p_192087_);
                    this.fillColumnDown(p_192083_, Blocks.NETHER_BRICKS.defaultBlockState(), 18 - k, -1, l, p_192087_);
                }
            }
        }
    }

    public static class BridgeEndFiller extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 10;
        private static final int DEPTH = 8;
        private final int selfSeed;

        public BridgeEndFiller(int pGenDepth, Random pRandom, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, pGenDepth, pBox);
            this.setOrientation(pOrientation);
            this.selfSeed = pRandom.nextInt();
        }

        public BridgeEndFiller(CompoundTag p_192091_)
        {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, p_192091_);
            this.selfSeed = p_192091_.getInt("Seed");
        }

        public static NetherBridgePieces.BridgeEndFiller createPiece(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, -3, 0, 5, 10, 8, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.BridgeEndFiller(pGenDepth, pRandom, boundingbox, pOrientation) : null;
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192101_, CompoundTag p_192102_)
        {
            super.addAdditionalSaveData(p_192101_, p_192102_);
            p_192102_.putInt("Seed", this.selfSeed);
        }

        public void postProcess(WorldGenLevel p_192093_, StructureFeatureManager p_192094_, ChunkGenerator p_192095_, Random p_192096_, BoundingBox p_192097_, ChunkPos p_192098_, BlockPos p_192099_)
        {
            Random random = new Random((long)this.selfSeed);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 3; j <= 4; ++j)
                {
                    int k = random.nextInt(8);
                    this.generateBox(p_192093_, p_192097_, i, j, 0, i, j, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
            }

            int l = random.nextInt(8);
            this.generateBox(p_192093_, p_192097_, 0, 5, 0, 0, 5, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            l = random.nextInt(8);
            this.generateBox(p_192093_, p_192097_, 4, 5, 0, 4, 5, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int i1 = 0; i1 <= 4; ++i1)
            {
                int k1 = random.nextInt(5);
                this.generateBox(p_192093_, p_192097_, i1, 2, 0, i1, 2, k1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }

            for (int j1 = 0; j1 <= 4; ++j1)
            {
                for (int l1 = 0; l1 <= 1; ++l1)
                {
                    int i2 = random.nextInt(3);
                    this.generateBox(p_192093_, p_192097_, j1, l1, 0, j1, l1, i2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
            }
        }
    }

    public static class BridgeStraight extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 10;
        private static final int DEPTH = 19;

        public BridgeStraight(int pGenDepth, Random pRandom, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public BridgeStraight(CompoundTag p_192104_)
        {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, p_192104_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 1, 3, false);
        }

        public static NetherBridgePieces.BridgeStraight createPiece(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, -3, 0, 5, 10, 19, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.BridgeStraight(pGenDepth, pRandom, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192106_, StructureFeatureManager p_192107_, ChunkGenerator p_192108_, Random p_192109_, BoundingBox p_192110_, ChunkPos p_192111_, BlockPos p_192112_)
        {
            this.generateBox(p_192106_, p_192110_, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192106_, p_192110_, 1, 5, 0, 3, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192106_, p_192110_, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192106_, p_192110_, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192106_, p_192110_, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192106_, p_192110_, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192106_, p_192110_, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192106_, p_192110_, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 2; ++j)
                {
                    this.fillColumnDown(p_192106_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192110_);
                    this.fillColumnDown(p_192106_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - j, p_192110_);
                }
            }

            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            BlockState blockstate2 = blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate = blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true));
            this.generateBox(p_192106_, p_192110_, 0, 1, 1, 0, 4, 1, blockstate2, blockstate2, false);
            this.generateBox(p_192106_, p_192110_, 0, 3, 4, 0, 4, 4, blockstate2, blockstate2, false);
            this.generateBox(p_192106_, p_192110_, 0, 3, 14, 0, 4, 14, blockstate2, blockstate2, false);
            this.generateBox(p_192106_, p_192110_, 0, 1, 17, 0, 4, 17, blockstate2, blockstate2, false);
            this.generateBox(p_192106_, p_192110_, 4, 1, 1, 4, 4, 1, blockstate, blockstate, false);
            this.generateBox(p_192106_, p_192110_, 4, 3, 4, 4, 4, 4, blockstate, blockstate, false);
            this.generateBox(p_192106_, p_192110_, 4, 3, 14, 4, 4, 14, blockstate, blockstate, false);
            this.generateBox(p_192106_, p_192110_, 4, 1, 17, 4, 4, 17, blockstate, blockstate, false);
        }
    }

    public static class CastleCorridorStairsPiece extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 14;
        private static final int DEPTH = 10;

        public CastleCorridorStairsPiece(int pGenDepth, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public CastleCorridorStairsPiece(CompoundTag p_192114_)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, p_192114_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 1, 0, true);
        }

        public static NetherBridgePieces.CastleCorridorStairsPiece createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, -7, 0, 5, 14, 10, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.CastleCorridorStairsPiece(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192116_, StructureFeatureManager p_192117_, ChunkGenerator p_192118_, Random p_192119_, BoundingBox p_192120_, ChunkPos p_192121_, BlockPos p_192122_)
        {
            BlockState blockstate = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));

            for (int i = 0; i <= 9; ++i)
            {
                int j = Math.max(1, 7 - i);
                int k = Math.min(Math.max(j + 5, 14 - i), 13);
                int l = i;
                this.generateBox(p_192116_, p_192120_, 0, 0, i, 4, j, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                this.generateBox(p_192116_, p_192120_, 1, j + 1, i, 3, k - 1, i, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);

                if (i <= 6)
                {
                    this.placeBlock(p_192116_, blockstate, 1, j + 1, i, p_192120_);
                    this.placeBlock(p_192116_, blockstate, 2, j + 1, i, p_192120_);
                    this.placeBlock(p_192116_, blockstate, 3, j + 1, i, p_192120_);
                }

                this.generateBox(p_192116_, p_192120_, 0, k, i, 4, k, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                this.generateBox(p_192116_, p_192120_, 0, j + 1, i, 0, k - 1, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                this.generateBox(p_192116_, p_192120_, 4, j + 1, i, 4, k - 1, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

                if ((i & 1) == 0)
                {
                    this.generateBox(p_192116_, p_192120_, 0, j + 2, i, 0, j + 3, i, blockstate1, blockstate1, false);
                    this.generateBox(p_192116_, p_192120_, 4, j + 2, i, 4, j + 3, i, blockstate1, blockstate1, false);
                }

                for (int i1 = 0; i1 <= 4; ++i1)
                {
                    this.fillColumnDown(p_192116_, Blocks.NETHER_BRICKS.defaultBlockState(), i1, -1, l, p_192120_);
                }
            }
        }
    }

    public static class CastleCorridorTBalconyPiece extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 9;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 9;

        public CastleCorridorTBalconyPiece(int pGenDepth, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public CastleCorridorTBalconyPiece(CompoundTag p_192124_)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, p_192124_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            int i = 1;
            Direction direction = this.getOrientation();

            if (direction == Direction.WEST || direction == Direction.NORTH)
            {
                i = 5;
            }

            this.generateChildLeft((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 0, i, pRandom.nextInt(8) > 0);
            this.generateChildRight((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 0, i, pRandom.nextInt(8) > 0);
        }

        public static NetherBridgePieces.CastleCorridorTBalconyPiece createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -3, 0, 0, 9, 7, 9, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.CastleCorridorTBalconyPiece(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192126_, StructureFeatureManager p_192127_, ChunkGenerator p_192128_, Random p_192129_, BoundingBox p_192130_, ChunkPos p_192131_, BlockPos p_192132_)
        {
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            this.generateBox(p_192126_, p_192130_, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 0, 2, 0, 8, 5, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 1, 3, 0, 1, 4, 0, blockstate1, blockstate1, false);
            this.generateBox(p_192126_, p_192130_, 7, 3, 0, 7, 4, 0, blockstate1, blockstate1, false);
            this.generateBox(p_192126_, p_192130_, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 1, 1, 4, 2, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 6, 1, 4, 7, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 1, 3, 8, 7, 3, 8, blockstate1, blockstate1, false);
            this.placeBlock(p_192126_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 0, 3, 8, p_192130_);
            this.placeBlock(p_192126_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 8, 3, 8, p_192130_);
            this.generateBox(p_192126_, p_192130_, 0, 3, 6, 0, 3, 7, blockstate, blockstate, false);
            this.generateBox(p_192126_, p_192130_, 8, 3, 6, 8, 3, 7, blockstate, blockstate, false);
            this.generateBox(p_192126_, p_192130_, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192126_, p_192130_, 1, 4, 5, 1, 5, 5, blockstate1, blockstate1, false);
            this.generateBox(p_192126_, p_192130_, 7, 4, 5, 7, 5, 5, blockstate1, blockstate1, false);

            for (int i = 0; i <= 5; ++i)
            {
                for (int j = 0; j <= 8; ++j)
                {
                    this.fillColumnDown(p_192126_, Blocks.NETHER_BRICKS.defaultBlockState(), j, -1, i, p_192130_);
                }
            }
        }
    }

    public static class CastleEntrance extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 13;
        private static final int HEIGHT = 14;
        private static final int DEPTH = 13;

        public CastleEntrance(int pGenDepth, Random pRandom, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public CastleEntrance(CompoundTag p_192134_)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, p_192134_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 5, 3, true);
        }

        public static NetherBridgePieces.CastleEntrance createPiece(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -5, -3, 0, 13, 14, 13, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.CastleEntrance(pGenDepth, pRandom, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192136_, StructureFeatureManager p_192137_, ChunkGenerator p_192138_, Random p_192139_, BoundingBox p_192140_, ChunkPos p_192141_, BlockPos p_192142_)
        {
            this.generateBox(p_192136_, p_192140_, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.defaultBlockState(), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), false);
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));

            for (int i = 1; i <= 11; i += 2)
            {
                this.generateBox(p_192136_, p_192140_, i, 10, 0, i, 11, 0, blockstate, blockstate, false);
                this.generateBox(p_192136_, p_192140_, i, 10, 12, i, 11, 12, blockstate, blockstate, false);
                this.generateBox(p_192136_, p_192140_, 0, 10, i, 0, 11, i, blockstate1, blockstate1, false);
                this.generateBox(p_192136_, p_192140_, 12, 10, i, 12, 11, i, blockstate1, blockstate1, false);
                this.placeBlock(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, p_192140_);
                this.placeBlock(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, p_192140_);
                this.placeBlock(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, p_192140_);
                this.placeBlock(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, p_192140_);

                if (i != 11)
                {
                    this.placeBlock(p_192136_, blockstate, i + 1, 13, 0, p_192140_);
                    this.placeBlock(p_192136_, blockstate, i + 1, 13, 12, p_192140_);
                    this.placeBlock(p_192136_, blockstate1, 0, 13, i + 1, p_192140_);
                    this.placeBlock(p_192136_, blockstate1, 12, 13, i + 1, p_192140_);
                }
            }

            this.placeBlock(p_192136_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 0, p_192140_);
            this.placeBlock(p_192136_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 12, p_192140_);
            this.placeBlock(p_192136_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 12, p_192140_);
            this.placeBlock(p_192136_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 0, p_192140_);

            for (int k = 3; k <= 9; k += 2)
            {
                this.generateBox(p_192136_, p_192140_, 1, 7, k, 1, 8, k, blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), false);
                this.generateBox(p_192136_, p_192140_, 11, 7, k, 11, 8, k, blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), false);
            }

            this.generateBox(p_192136_, p_192140_, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int l = 4; l <= 8; ++l)
            {
                for (int j = 0; j <= 2; ++j)
                {
                    this.fillColumnDown(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), l, -1, j, p_192140_);
                    this.fillColumnDown(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), l, -1, 12 - j, p_192140_);
                }
            }

            for (int i1 = 0; i1 <= 2; ++i1)
            {
                for (int j1 = 4; j1 <= 8; ++j1)
                {
                    this.fillColumnDown(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), i1, -1, j1, p_192140_);
                    this.fillColumnDown(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i1, -1, j1, p_192140_);
                }
            }

            this.generateBox(p_192136_, p_192140_, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192136_, p_192140_, 6, 1, 6, 6, 4, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.placeBlock(p_192136_, Blocks.NETHER_BRICKS.defaultBlockState(), 6, 0, 6, p_192140_);
            this.placeBlock(p_192136_, Blocks.LAVA.defaultBlockState(), 6, 5, 6, p_192140_);
            BlockPos blockpos = this.getWorldPos(6, 5, 6);

            if (p_192140_.isInside(blockpos))
            {
                p_192136_.scheduleTick(blockpos, Fluids.LAVA, 0);
            }
        }
    }

    public static class CastleSmallCorridorCrossingPiece extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 5;

        public CastleSmallCorridorCrossingPiece(int pGenDepth, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public CastleSmallCorridorCrossingPiece(CompoundTag p_192144_)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, p_192144_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 1, 0, true);
            this.generateChildLeft((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 0, 1, true);
            this.generateChildRight((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 0, 1, true);
        }

        public static NetherBridgePieces.CastleSmallCorridorCrossingPiece createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, 0, 0, 5, 7, 5, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.CastleSmallCorridorCrossingPiece(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192146_, StructureFeatureManager p_192147_, ChunkGenerator p_192148_, Random p_192149_, BoundingBox p_192150_, ChunkPos p_192151_, BlockPos p_192152_)
        {
            this.generateBox(p_192146_, p_192150_, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192146_, p_192150_, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192146_, p_192150_, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192146_, p_192150_, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192146_, p_192150_, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192146_, p_192150_, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192146_, p_192150_, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 4; ++j)
                {
                    this.fillColumnDown(p_192146_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192150_);
                }
            }
        }
    }

    public static class CastleSmallCorridorLeftTurnPiece extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 5;
        private boolean isNeedingChest;

        public CastleSmallCorridorLeftTurnPiece(int pGenDepth, Random pRandom, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, pGenDepth, pBox);
            this.setOrientation(pOrientation);
            this.isNeedingChest = pRandom.nextInt(3) == 0;
        }

        public CastleSmallCorridorLeftTurnPiece(CompoundTag p_192154_)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, p_192154_);
            this.isNeedingChest = p_192154_.getBoolean("Chest");
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192164_, CompoundTag p_192165_)
        {
            super.addAdditionalSaveData(p_192164_, p_192165_);
            p_192165_.putBoolean("Chest", this.isNeedingChest);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildLeft((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 0, 1, true);
        }

        public static NetherBridgePieces.CastleSmallCorridorLeftTurnPiece createPiece(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, 0, 0, 5, 7, 5, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.CastleSmallCorridorLeftTurnPiece(pGenDepth, pRandom, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192156_, StructureFeatureManager p_192157_, ChunkGenerator p_192158_, Random p_192159_, BoundingBox p_192160_, ChunkPos p_192161_, BlockPos p_192162_)
        {
            this.generateBox(p_192156_, p_192160_, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192156_, p_192160_, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(p_192156_, p_192160_, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192156_, p_192160_, 4, 3, 1, 4, 4, 1, blockstate1, blockstate1, false);
            this.generateBox(p_192156_, p_192160_, 4, 3, 3, 4, 4, 3, blockstate1, blockstate1, false);
            this.generateBox(p_192156_, p_192160_, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192156_, p_192160_, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192156_, p_192160_, 1, 3, 4, 1, 4, 4, blockstate, blockstate, false);
            this.generateBox(p_192156_, p_192160_, 3, 3, 4, 3, 4, 4, blockstate, blockstate, false);

            if (this.isNeedingChest && p_192160_.isInside(this.getWorldPos(3, 2, 3)))
            {
                this.isNeedingChest = false;
                this.createChest(p_192156_, p_192160_, p_192159_, 3, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
            }

            this.generateBox(p_192156_, p_192160_, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 4; ++j)
                {
                    this.fillColumnDown(p_192156_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192160_);
                }
            }
        }
    }

    public static class CastleSmallCorridorPiece extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 5;

        public CastleSmallCorridorPiece(int pGenDepth, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public CastleSmallCorridorPiece(CompoundTag p_192167_)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, p_192167_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 1, 0, true);
        }

        public static NetherBridgePieces.CastleSmallCorridorPiece createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, 0, 0, 5, 7, 5, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.CastleSmallCorridorPiece(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192169_, StructureFeatureManager p_192170_, ChunkGenerator p_192171_, Random p_192172_, BoundingBox p_192173_, ChunkPos p_192174_, BlockPos p_192175_)
        {
            this.generateBox(p_192169_, p_192173_, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192169_, p_192173_, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(p_192169_, p_192173_, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192169_, p_192173_, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192169_, p_192173_, 0, 3, 1, 0, 4, 1, blockstate, blockstate, false);
            this.generateBox(p_192169_, p_192173_, 0, 3, 3, 0, 4, 3, blockstate, blockstate, false);
            this.generateBox(p_192169_, p_192173_, 4, 3, 1, 4, 4, 1, blockstate, blockstate, false);
            this.generateBox(p_192169_, p_192173_, 4, 3, 3, 4, 4, 3, blockstate, blockstate, false);
            this.generateBox(p_192169_, p_192173_, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 4; ++j)
                {
                    this.fillColumnDown(p_192169_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192173_);
                }
            }
        }
    }

    public static class CastleSmallCorridorRightTurnPiece extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 5;
        private boolean isNeedingChest;

        public CastleSmallCorridorRightTurnPiece(int pGenDepth, Random pRandom, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, pGenDepth, pBox);
            this.setOrientation(pOrientation);
            this.isNeedingChest = pRandom.nextInt(3) == 0;
        }

        public CastleSmallCorridorRightTurnPiece(CompoundTag p_192177_)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, p_192177_);
            this.isNeedingChest = p_192177_.getBoolean("Chest");
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192187_, CompoundTag p_192188_)
        {
            super.addAdditionalSaveData(p_192187_, p_192188_);
            p_192188_.putBoolean("Chest", this.isNeedingChest);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildRight((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 0, 1, true);
        }

        public static NetherBridgePieces.CastleSmallCorridorRightTurnPiece createPiece(StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, 0, 0, 5, 7, 5, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.CastleSmallCorridorRightTurnPiece(pGenDepth, pRandom, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192179_, StructureFeatureManager p_192180_, ChunkGenerator p_192181_, Random p_192182_, BoundingBox p_192183_, ChunkPos p_192184_, BlockPos p_192185_)
        {
            this.generateBox(p_192179_, p_192183_, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192179_, p_192183_, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(p_192179_, p_192183_, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192179_, p_192183_, 0, 3, 1, 0, 4, 1, blockstate1, blockstate1, false);
            this.generateBox(p_192179_, p_192183_, 0, 3, 3, 0, 4, 3, blockstate1, blockstate1, false);
            this.generateBox(p_192179_, p_192183_, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192179_, p_192183_, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192179_, p_192183_, 1, 3, 4, 1, 4, 4, blockstate, blockstate, false);
            this.generateBox(p_192179_, p_192183_, 3, 3, 4, 3, 4, 4, blockstate, blockstate, false);

            if (this.isNeedingChest && p_192183_.isInside(this.getWorldPos(1, 2, 3)))
            {
                this.isNeedingChest = false;
                this.createChest(p_192179_, p_192183_, p_192182_, 1, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
            }

            this.generateBox(p_192179_, p_192183_, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 4; ++j)
                {
                    this.fillColumnDown(p_192179_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192183_);
                }
            }
        }
    }

    public static class CastleStalkRoom extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 13;
        private static final int HEIGHT = 14;
        private static final int DEPTH = 13;

        public CastleStalkRoom(int pGenDepth, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public CastleStalkRoom(CompoundTag p_192190_)
        {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, p_192190_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 5, 3, true);
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 5, 11, true);
        }

        public static NetherBridgePieces.CastleStalkRoom createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -5, -3, 0, 13, 14, 13, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.CastleStalkRoom(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192192_, StructureFeatureManager p_192193_, ChunkGenerator p_192194_, Random p_192195_, BoundingBox p_192196_, ChunkPos p_192197_, BlockPos p_192198_)
        {
            this.generateBox(p_192192_, p_192196_, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            BlockState blockstate2 = blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true));
            BlockState blockstate3 = blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true));

            for (int i = 1; i <= 11; i += 2)
            {
                this.generateBox(p_192192_, p_192196_, i, 10, 0, i, 11, 0, blockstate, blockstate, false);
                this.generateBox(p_192192_, p_192196_, i, 10, 12, i, 11, 12, blockstate, blockstate, false);
                this.generateBox(p_192192_, p_192196_, 0, 10, i, 0, 11, i, blockstate1, blockstate1, false);
                this.generateBox(p_192192_, p_192196_, 12, 10, i, 12, 11, i, blockstate1, blockstate1, false);
                this.placeBlock(p_192192_, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, p_192196_);
                this.placeBlock(p_192192_, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, p_192196_);
                this.placeBlock(p_192192_, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, p_192196_);
                this.placeBlock(p_192192_, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, p_192196_);

                if (i != 11)
                {
                    this.placeBlock(p_192192_, blockstate, i + 1, 13, 0, p_192196_);
                    this.placeBlock(p_192192_, blockstate, i + 1, 13, 12, p_192196_);
                    this.placeBlock(p_192192_, blockstate1, 0, 13, i + 1, p_192196_);
                    this.placeBlock(p_192192_, blockstate1, 12, 13, i + 1, p_192196_);
                }
            }

            this.placeBlock(p_192192_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 0, p_192196_);
            this.placeBlock(p_192192_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 12, p_192196_);
            this.placeBlock(p_192192_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 12, p_192196_);
            this.placeBlock(p_192192_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 0, p_192196_);

            for (int j1 = 3; j1 <= 9; j1 += 2)
            {
                this.generateBox(p_192192_, p_192196_, 1, 7, j1, 1, 8, j1, blockstate2, blockstate2, false);
                this.generateBox(p_192192_, p_192196_, 11, 7, j1, 11, 8, j1, blockstate3, blockstate3, false);
            }

            BlockState blockstate4 = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);

            for (int j = 0; j <= 6; ++j)
            {
                int k = j + 4;

                for (int l = 5; l <= 7; ++l)
                {
                    this.placeBlock(p_192192_, blockstate4, l, 5 + j, k, p_192196_);
                }

                if (k >= 5 && k <= 8)
                {
                    this.generateBox(p_192192_, p_192196_, 5, 5, k, 7, j + 4, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
                else if (k >= 9 && k <= 10)
                {
                    this.generateBox(p_192192_, p_192196_, 5, 8, k, 7, j + 4, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }

                if (j >= 1)
                {
                    this.generateBox(p_192192_, p_192196_, 5, 6 + j, k, 7, 9 + j, k, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
                }
            }

            for (int k1 = 5; k1 <= 7; ++k1)
            {
                this.placeBlock(p_192192_, blockstate4, k1, 12, 11, p_192196_);
            }

            this.generateBox(p_192192_, p_192196_, 5, 6, 7, 5, 7, 7, blockstate3, blockstate3, false);
            this.generateBox(p_192192_, p_192196_, 7, 6, 7, 7, 7, 7, blockstate2, blockstate2, false);
            this.generateBox(p_192192_, p_192196_, 5, 13, 12, 7, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockstate5 = blockstate4.setValue(StairBlock.FACING, Direction.EAST);
            BlockState blockstate6 = blockstate4.setValue(StairBlock.FACING, Direction.WEST);
            this.placeBlock(p_192192_, blockstate6, 4, 5, 2, p_192196_);
            this.placeBlock(p_192192_, blockstate6, 4, 5, 3, p_192196_);
            this.placeBlock(p_192192_, blockstate6, 4, 5, 9, p_192196_);
            this.placeBlock(p_192192_, blockstate6, 4, 5, 10, p_192196_);
            this.placeBlock(p_192192_, blockstate5, 8, 5, 2, p_192196_);
            this.placeBlock(p_192192_, blockstate5, 8, 5, 3, p_192196_);
            this.placeBlock(p_192192_, blockstate5, 8, 5, 9, p_192196_);
            this.placeBlock(p_192192_, blockstate5, 8, 5, 10, p_192196_);
            this.generateBox(p_192192_, p_192196_, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192192_, p_192196_, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for (int l1 = 4; l1 <= 8; ++l1)
            {
                for (int i1 = 0; i1 <= 2; ++i1)
                {
                    this.fillColumnDown(p_192192_, Blocks.NETHER_BRICKS.defaultBlockState(), l1, -1, i1, p_192196_);
                    this.fillColumnDown(p_192192_, Blocks.NETHER_BRICKS.defaultBlockState(), l1, -1, 12 - i1, p_192196_);
                }
            }

            for (int i2 = 0; i2 <= 2; ++i2)
            {
                for (int j2 = 4; j2 <= 8; ++j2)
                {
                    this.fillColumnDown(p_192192_, Blocks.NETHER_BRICKS.defaultBlockState(), i2, -1, j2, p_192196_);
                    this.fillColumnDown(p_192192_, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i2, -1, j2, p_192196_);
                }
            }
        }
    }

    public static class MonsterThrone extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 7;
        private static final int HEIGHT = 8;
        private static final int DEPTH = 9;
        private boolean hasPlacedSpawner;

        public MonsterThrone(int pGenDepth, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public MonsterThrone(CompoundTag p_192200_)
        {
            super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, p_192200_);
            this.hasPlacedSpawner = p_192200_.getBoolean("Mob");
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192210_, CompoundTag p_192211_)
        {
            super.addAdditionalSaveData(p_192210_, p_192211_);
            p_192211_.putBoolean("Mob", this.hasPlacedSpawner);
        }

        public static NetherBridgePieces.MonsterThrone createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, int pGenDepth, Direction pOrientation)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -2, 0, 0, 7, 8, 9, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.MonsterThrone(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192202_, StructureFeatureManager p_192203_, ChunkGenerator p_192204_, Random p_192205_, BoundingBox p_192206_, ChunkPos p_192207_, BlockPos p_192208_)
        {
            this.generateBox(p_192202_, p_192206_, 0, 2, 0, 6, 7, 7, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192202_, p_192206_, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 1, 6, 3, p_192206_);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 5, 6, 3, p_192206_);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 0, 6, 3, p_192206_);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 6, 6, 3, p_192206_);
            this.generateBox(p_192202_, p_192206_, 0, 6, 4, 0, 6, 7, blockstate1, blockstate1, false);
            this.generateBox(p_192202_, p_192206_, 6, 6, 4, 6, 6, 7, blockstate1, blockstate1, false);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 0, 6, 8, p_192206_);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 6, 6, 8, p_192206_);
            this.generateBox(p_192202_, p_192206_, 1, 6, 8, 5, 6, 8, blockstate, blockstate, false);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 1, 7, 8, p_192206_);
            this.generateBox(p_192202_, p_192206_, 2, 7, 8, 4, 7, 8, blockstate, blockstate, false);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 5, 7, 8, p_192206_);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 2, 8, 8, p_192206_);
            this.placeBlock(p_192202_, blockstate, 3, 8, 8, p_192206_);
            this.placeBlock(p_192202_, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 4, 8, 8, p_192206_);

            if (!this.hasPlacedSpawner)
            {
                BlockPos blockpos = this.getWorldPos(3, 5, 5);

                if (p_192206_.isInside(blockpos))
                {
                    this.hasPlacedSpawner = true;
                    p_192202_.setBlock(blockpos, Blocks.SPAWNER.defaultBlockState(), 2);
                    BlockEntity blockentity = p_192202_.getBlockEntity(blockpos);

                    if (blockentity instanceof SpawnerBlockEntity)
                    {
                        ((SpawnerBlockEntity)blockentity).getSpawner().setEntityId(EntityType.BLAZE);
                    }
                }
            }

            for (int i = 0; i <= 6; ++i)
            {
                for (int j = 0; j <= 6; ++j)
                {
                    this.fillColumnDown(p_192202_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192206_);
                }
            }
        }
    }

    abstract static class NetherBridgePiece extends StructurePiece
    {
        protected NetherBridgePiece(StructurePieceType p_209887_, int p_209888_, BoundingBox p_209889_)
        {
            super(p_209887_, p_209888_, p_209889_);
        }

        public NetherBridgePiece(StructurePieceType p_209891_, CompoundTag p_209892_)
        {
            super(p_209891_, p_209892_);
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_192213_, CompoundTag p_192214_)
        {
        }

        private int updatePieceWeight(List<NetherBridgePieces.PieceWeight> pWeights)
        {
            boolean flag = false;
            int i = 0;

            for (NetherBridgePieces.PieceWeight netherbridgepieces$pieceweight : pWeights)
            {
                if (netherbridgepieces$pieceweight.maxPlaceCount > 0 && netherbridgepieces$pieceweight.placeCount < netherbridgepieces$pieceweight.maxPlaceCount)
                {
                    flag = true;
                }

                i += netherbridgepieces$pieceweight.weight;
            }

            return flag ? i : -1;
        }

        private NetherBridgePieces.NetherBridgePiece generatePiece(NetherBridgePieces.StartPiece pStartPiece, List<NetherBridgePieces.PieceWeight> pWeights, StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            int i = this.updatePieceWeight(pWeights);
            boolean flag = i > 0 && pGenDepth <= 30;
            int j = 0;

            while (j < 5 && flag)
            {
                ++j;
                int k = pRandom.nextInt(i);

                for (NetherBridgePieces.PieceWeight netherbridgepieces$pieceweight : pWeights)
                {
                    k -= netherbridgepieces$pieceweight.weight;

                    if (k < 0)
                    {
                        if (!netherbridgepieces$pieceweight.doPlace(pGenDepth) || netherbridgepieces$pieceweight == pStartPiece.previousPiece && !netherbridgepieces$pieceweight.allowInRow)
                        {
                            break;
                        }

                        NetherBridgePieces.NetherBridgePiece netherbridgepieces$netherbridgepiece = NetherBridgePieces.findAndCreateBridgePieceFactory(netherbridgepieces$pieceweight, pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);

                        if (netherbridgepieces$netherbridgepiece != null)
                        {
                            ++netherbridgepieces$pieceweight.placeCount;
                            pStartPiece.previousPiece = netherbridgepieces$pieceweight;

                            if (!netherbridgepieces$pieceweight.isValid())
                            {
                                pWeights.remove(netherbridgepieces$pieceweight);
                            }

                            return netherbridgepieces$netherbridgepiece;
                        }
                    }
                }
            }

            return NetherBridgePieces.BridgeEndFiller.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
        }

        private StructurePiece generateAndAddPiece(NetherBridgePieces.StartPiece pStartPiece, StructurePieceAccessor pPieces, Random pRandom, int pX, int pY, int pZ, @Nullable Direction pOrientation, int pGenDepth, boolean pCastlePiece)
        {
            if (Math.abs(pX - pStartPiece.getBoundingBox().minX()) <= 112 && Math.abs(pZ - pStartPiece.getBoundingBox().minZ()) <= 112)
            {
                List<NetherBridgePieces.PieceWeight> list = pStartPiece.availableBridgePieces;

                if (pCastlePiece)
                {
                    list = pStartPiece.availableCastlePieces;
                }

                StructurePiece structurepiece = this.generatePiece(pStartPiece, list, pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth + 1);

                if (structurepiece != null)
                {
                    pPieces.addPiece(structurepiece);
                    pStartPiece.pendingChildren.add(structurepiece);
                }

                return structurepiece;
            }
            else
            {
                return NetherBridgePieces.BridgeEndFiller.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
            }
        }

        @Nullable
        protected StructurePiece generateChildForward(NetherBridgePieces.StartPiece pStartPiece, StructurePieceAccessor pPieces, Random pRandom, int pOffsetX, int pOffsetY, boolean pCastlePiece)
        {
            Direction direction = this.getOrientation();

            if (direction != null)
            {
                switch (direction)
                {
                    case NORTH:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() - 1, direction, this.getGenDepth(), pCastlePiece);

                    case SOUTH:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.maxZ() + 1, direction, this.getGenDepth(), pCastlePiece);

                    case WEST:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, direction, this.getGenDepth(), pCastlePiece);

                    case EAST:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, direction, this.getGenDepth(), pCastlePiece);
                }
            }

            return null;
        }

        @Nullable
        protected StructurePiece generateChildLeft(NetherBridgePieces.StartPiece pStartPiece, StructurePieceAccessor pPieces, Random pRandom, int pOffsetY, int pOffsetX, boolean pCastlePiece)
        {
            Direction direction = this.getOrientation();

            if (direction != null)
            {
                switch (direction)
                {
                    case NORTH:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, Direction.WEST, this.getGenDepth(), pCastlePiece);

                    case SOUTH:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, Direction.WEST, this.getGenDepth(), pCastlePiece);

                    case WEST:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth(), pCastlePiece);

                    case EAST:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth(), pCastlePiece);
                }
            }

            return null;
        }

        @Nullable
        protected StructurePiece generateChildRight(NetherBridgePieces.StartPiece pStartPiece, StructurePieceAccessor pPieces, Random pRandom, int pOffsetY, int pOffsetX, boolean pCastlePiece)
        {
            Direction direction = this.getOrientation();

            if (direction != null)
            {
                switch (direction)
                {
                    case NORTH:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, Direction.EAST, this.getGenDepth(), pCastlePiece);

                    case SOUTH:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, Direction.EAST, this.getGenDepth(), pCastlePiece);

                    case WEST:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth(), pCastlePiece);

                    case EAST:
                        return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth(), pCastlePiece);
                }
            }

            return null;
        }

        protected static boolean isOkBox(BoundingBox pBox)
        {
            return pBox != null && pBox.minY() > 10;
        }
    }

    static class PieceWeight
    {
        public final Class <? extends NetherBridgePieces.NetherBridgePiece > pieceClass;
        public final int weight;
        public int placeCount;
        public final int maxPlaceCount;
        public final boolean allowInRow;

        public PieceWeight(Class <? extends NetherBridgePieces.NetherBridgePiece > pPieceClass, int pWeight, int pMaxPlaceCount, boolean pAllowInRow)
        {
            this.pieceClass = pPieceClass;
            this.weight = pWeight;
            this.maxPlaceCount = pMaxPlaceCount;
            this.allowInRow = pAllowInRow;
        }

        public PieceWeight(Class <? extends NetherBridgePieces.NetherBridgePiece > pPieceClass, int pWeight, int pMaxPlaceCount)
        {
            this(pPieceClass, pWeight, pMaxPlaceCount, false);
        }

        public boolean doPlace(int p_71966_)
        {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }

        public boolean isValid()
        {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }
    }

    public static class RoomCrossing extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 7;
        private static final int HEIGHT = 9;
        private static final int DEPTH = 7;

        public RoomCrossing(int pGenDepth, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public RoomCrossing(CompoundTag p_192216_)
        {
            super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, p_192216_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildForward((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 2, 0, false);
            this.generateChildLeft((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 0, 2, false);
            this.generateChildRight((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 0, 2, false);
        }

        public static NetherBridgePieces.RoomCrossing createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -2, 0, 0, 7, 9, 7, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.RoomCrossing(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192218_, StructureFeatureManager p_192219_, ChunkGenerator p_192220_, Random p_192221_, BoundingBox p_192222_, ChunkPos p_192223_, BlockPos p_192224_)
        {
            this.generateBox(p_192218_, p_192222_, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 0, 2, 0, 6, 7, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(p_192218_, p_192222_, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 2, 5, 0, 4, 5, 0, blockstate, blockstate, false);
            this.generateBox(p_192218_, p_192222_, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 2, 5, 6, 4, 5, 6, blockstate, blockstate, false);
            this.generateBox(p_192218_, p_192222_, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 0, 5, 2, 0, 5, 4, blockstate1, blockstate1, false);
            this.generateBox(p_192218_, p_192222_, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192218_, p_192222_, 6, 5, 2, 6, 5, 4, blockstate1, blockstate1, false);

            for (int i = 0; i <= 6; ++i)
            {
                for (int j = 0; j <= 6; ++j)
                {
                    this.fillColumnDown(p_192218_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192222_);
                }
            }
        }
    }

    public static class StairsRoom extends NetherBridgePieces.NetherBridgePiece
    {
        private static final int WIDTH = 7;
        private static final int HEIGHT = 11;
        private static final int DEPTH = 7;

        public StairsRoom(int pGenDepth, BoundingBox pBox, Direction pOrientation)
        {
            super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, pGenDepth, pBox);
            this.setOrientation(pOrientation);
        }

        public StairsRoom(CompoundTag p_192226_)
        {
            super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, p_192226_);
        }

        public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, Random pRandom)
        {
            this.generateChildRight((NetherBridgePieces.StartPiece)pPiece, pPieces, pRandom, 6, 2, false);
        }

        public static NetherBridgePieces.StairsRoom createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, int pGenDepth, Direction pOrientation)
        {
            BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -2, 0, 0, 7, 11, 7, pOrientation);
            return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherBridgePieces.StairsRoom(pGenDepth, boundingbox, pOrientation) : null;
        }

        public void postProcess(WorldGenLevel p_192228_, StructureFeatureManager p_192229_, ChunkGenerator p_192230_, Random p_192231_, BoundingBox p_192232_, ChunkPos p_192233_, BlockPos p_192234_)
        {
            this.generateBox(p_192228_, p_192232_, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 0, 2, 0, 6, 10, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(p_192228_, p_192232_, 0, 3, 2, 0, 5, 4, blockstate1, blockstate1, false);
            this.generateBox(p_192228_, p_192232_, 6, 3, 2, 6, 5, 2, blockstate1, blockstate1, false);
            this.generateBox(p_192228_, p_192232_, 6, 3, 4, 6, 5, 4, blockstate1, blockstate1, false);
            this.placeBlock(p_192228_, Blocks.NETHER_BRICKS.defaultBlockState(), 5, 2, 5, p_192232_);
            this.generateBox(p_192228_, p_192232_, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 6, 8, 2, 6, 8, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(p_192228_, p_192232_, 2, 5, 0, 4, 5, 0, blockstate, blockstate, false);

            for (int i = 0; i <= 6; ++i)
            {
                for (int j = 0; j <= 6; ++j)
                {
                    this.fillColumnDown(p_192228_, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, p_192232_);
                }
            }
        }
    }

    public static class StartPiece extends NetherBridgePieces.BridgeCrossing
    {
        public NetherBridgePieces.PieceWeight previousPiece;
        public List<NetherBridgePieces.PieceWeight> availableBridgePieces;
        public List<NetherBridgePieces.PieceWeight> availableCastlePieces;
        public final List<StructurePiece> pendingChildren = Lists.newArrayList();

        public StartPiece(Random pRandom, int pX, int pZ)
        {
            super(pX, pZ, getRandomHorizontalDirection(pRandom));
            this.availableBridgePieces = Lists.newArrayList();

            for (NetherBridgePieces.PieceWeight netherbridgepieces$pieceweight : NetherBridgePieces.BRIDGE_PIECE_WEIGHTS)
            {
                netherbridgepieces$pieceweight.placeCount = 0;
                this.availableBridgePieces.add(netherbridgepieces$pieceweight);
            }

            this.availableCastlePieces = Lists.newArrayList();

            for (NetherBridgePieces.PieceWeight netherbridgepieces$pieceweight1 : NetherBridgePieces.CASTLE_PIECE_WEIGHTS)
            {
                netherbridgepieces$pieceweight1.placeCount = 0;
                this.availableCastlePieces.add(netherbridgepieces$pieceweight1);
            }
        }

        public StartPiece(CompoundTag p_192236_)
        {
            super(StructurePieceType.NETHER_FORTRESS_START, p_192236_);
        }
    }
}
