package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection
{
    public static final int SECTION_WIDTH = 16;
    public static final int SECTION_HEIGHT = 16;
    public static final int SECTION_SIZE = 4096;
    public static final int BIOME_CONTAINER_BITS = 2;
    private final int bottomBlockY;
    private short nonEmptyBlockCount;
    private short tickingBlockCount;
    private short tickingFluidCount;
    private final PalettedContainer<BlockState> states;
    private final PalettedContainer<Holder<Biome>> biomes;

    public LevelChunkSection(int p_187997_, PalettedContainer<BlockState> p_187998_, PalettedContainer<Holder<Biome>> p_187999_)
    {
        this.bottomBlockY = getBottomBlockY(p_187997_);
        this.states = p_187998_;
        this.biomes = p_187999_;
        this.recalcBlockCounts();
    }

    public LevelChunkSection(int p_188001_, Registry<Biome> p_188002_)
    {
        this.bottomBlockY = getBottomBlockY(p_188001_);
        this.states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        this.biomes = new PalettedContainer<>(p_188002_.asHolderIdMap(), p_188002_.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
    }

    public static int getBottomBlockY(int pSectionY)
    {
        return pSectionY << 4;
    }

    public BlockState getBlockState(int pX, int pY, int pZ)
    {
        return this.states.get(pX, pY, pZ);
    }

    public FluidState getFluidState(int pX, int pY, int pZ)
    {
        return this.states.get(pX, pY, pZ).getFluidState();
    }

    public void acquire()
    {
        this.states.acquire();
    }

    public void release()
    {
        this.states.release();
    }

    public BlockState setBlockState(int pX, int pY, int pZ, BlockState pState)
    {
        return this.setBlockState(pX, pY, pZ, pState, true);
    }

    public BlockState setBlockState(int pX, int pY, int pZ, BlockState pState, boolean pUseLocks)
    {
        BlockState blockstate;

        if (pUseLocks)
        {
            blockstate = this.states.getAndSet(pX, pY, pZ, pState);
        }
        else
        {
            blockstate = this.states.getAndSetUnchecked(pX, pY, pZ, pState);
        }

        FluidState fluidstate = blockstate.getFluidState();
        FluidState fluidstate1 = pState.getFluidState();

        if (!blockstate.isAir())
        {
            --this.nonEmptyBlockCount;

            if (blockstate.isRandomlyTicking())
            {
                --this.tickingBlockCount;
            }
        }

        if (!fluidstate.isEmpty())
        {
            --this.tickingFluidCount;
        }

        if (!pState.isAir())
        {
            ++this.nonEmptyBlockCount;

            if (pState.isRandomlyTicking())
            {
                ++this.tickingBlockCount;
            }
        }

        if (!fluidstate1.isEmpty())
        {
            ++this.tickingFluidCount;
        }

        return blockstate;
    }

    public boolean hasOnlyAir()
    {
        return this.nonEmptyBlockCount == 0;
    }

    public boolean isRandomlyTicking()
    {
        return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
    }

    public boolean isRandomlyTickingBlocks()
    {
        return this.tickingBlockCount > 0;
    }

    public boolean isRandomlyTickingFluids()
    {
        return this.tickingFluidCount > 0;
    }

    public int bottomBlockY()
    {
        return this.bottomBlockY;
    }

    public void recalcBlockCounts()
    {
        LevelChunkSection$1BlockCounter levelchunksection$1blockcounter = new LevelChunkSection$1BlockCounter(this);
        this.states.count(levelchunksection$1blockcounter);
        this.nonEmptyBlockCount = (short)levelchunksection$1blockcounter.nonEmptyBlockCount;
        this.tickingBlockCount = (short)levelchunksection$1blockcounter.tickingBlockCount;
        this.tickingFluidCount = (short)levelchunksection$1blockcounter.tickingFluidCount;
    }

    public PalettedContainer<BlockState> getStates()
    {
        return this.states;
    }

    public PalettedContainer<Holder<Biome>> getBiomes()
    {
        return this.biomes;
    }

    public void read(FriendlyByteBuf pBuffer)
    {
        this.nonEmptyBlockCount = pBuffer.readShort();
        this.states.read(pBuffer);
        this.biomes.read(pBuffer);
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeShort(this.nonEmptyBlockCount);
        this.states.write(pBuffer);
        this.biomes.write(pBuffer);
    }

    public int getSerializedSize()
    {
        return 2 + this.states.getSerializedSize() + this.biomes.getSerializedSize();
    }

    public boolean maybeHas(Predicate<BlockState> pPredicate)
    {
        return this.states.maybeHas(pPredicate);
    }

    public Holder<Biome> getNoiseBiome(int p_204434_, int p_204435_, int p_204436_)
    {
        return this.biomes.get(p_204434_, p_204435_, p_204436_);
    }

    public void fillBiomesFromNoise(BiomeResolver p_188004_, Climate.Sampler p_188005_, int p_188006_, int p_188007_)
    {
        PalettedContainer<Holder<Biome>> palettedcontainer = this.getBiomes();
        palettedcontainer.acquire();

        try
        {
            int i = QuartPos.fromBlock(this.bottomBlockY());
            int j = 4;

            for (int k = 0; k < 4; ++k)
            {
                for (int l = 0; l < 4; ++l)
                {
                    for (int i1 = 0; i1 < 4; ++i1)
                    {
                        palettedcontainer.getAndSetUnchecked(k, l, i1, p_188004_.getNoiseBiome(p_188006_ + k, i + l, p_188007_ + i1, p_188005_));
                    }
                }
            }
        }
        finally
        {
            palettedcontainer.release();
        }
    }

    public short getBlockRefCount()
    {
        return this.nonEmptyBlockCount;
    }
}
