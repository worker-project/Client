package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class RenderChunkRegion implements BlockAndTintGetter
{
    private final int centerX;
    private final int centerZ;
    protected final RenderChunk[][] chunks;
    protected final Level level;

    RenderChunkRegion(Level p_200456_, int p_200457_, int p_200458_, RenderChunk[][] p_200459_)
    {
        this.level = p_200456_;
        this.centerX = p_200457_;
        this.centerZ = p_200458_;
        this.chunks = p_200459_;
    }

    public BlockState getBlockState(BlockPos pPos)
    {
        int i = SectionPos.blockToSectionCoord(pPos.getX()) - this.centerX;
        int j = SectionPos.blockToSectionCoord(pPos.getZ()) - this.centerZ;
        return this.chunks[i][j].getBlockState(pPos);
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        int i = SectionPos.blockToSectionCoord(pPos.getX()) - this.centerX;
        int j = SectionPos.blockToSectionCoord(pPos.getZ()) - this.centerZ;
        return this.chunks[i][j].getBlockState(pPos).getFluidState();
    }

    public float getShade(Direction pDirection, boolean pShade)
    {
        return this.level.getShade(pDirection, pShade);
    }

    public LevelLightEngine getLightEngine()
    {
        return this.level.getLightEngine();
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos)
    {
        int i = SectionPos.blockToSectionCoord(pPos.getX()) - this.centerX;
        int j = SectionPos.blockToSectionCoord(pPos.getZ()) - this.centerZ;
        return this.chunks[i][j].getBlockEntity(pPos);
    }

    public int getBlockTint(BlockPos pPos, ColorResolver pColorResolver)
    {
        return this.level.getBlockTint(pPos, pColorResolver);
    }

    public int getMinBuildHeight()
    {
        return this.level.getMinBuildHeight();
    }

    public int getHeight()
    {
        return this.level.getHeight();
    }

    public Biome getBiome(BlockPos pos)
    {
        return this.level.getBiome(pos).value();
    }

    public LevelChunk getChunk(int ix, int iz)
    {
        return this.chunks[ix][iz].getChunk();
    }
}
