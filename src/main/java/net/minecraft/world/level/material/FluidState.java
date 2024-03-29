package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FluidState extends StateHolder<Fluid, FluidState>
{
    public static final Codec<FluidState> CODEC = codec(Registry.FLUID.byNameCodec(), Fluid::defaultFluidState).stable();
    public static final int AMOUNT_MAX = 9;
    public static final int AMOUNT_FULL = 8;

    public FluidState(Fluid p_76149_, ImmutableMap < Property<?>, Comparable<? >> p_76150_, MapCodec<FluidState> p_76151_)
    {
        super(p_76149_, p_76150_, p_76151_);
    }

    public Fluid getType()
    {
        return this.owner;
    }

    public boolean isSource()
    {
        return this.getType().isSource(this);
    }

    public boolean isSourceOfType(Fluid p_164513_)
    {
        return this.owner == p_164513_ && this.owner.isSource(this);
    }

    public boolean isEmpty()
    {
        return this.getType().isEmpty();
    }

    public float getHeight(BlockGetter p_76156_, BlockPos p_76157_)
    {
        return this.getType().getHeight(this, p_76156_, p_76157_);
    }

    public float getOwnHeight()
    {
        return this.getType().getOwnHeight(this);
    }

    public int getAmount()
    {
        return this.getType().getAmount(this);
    }

    public boolean shouldRenderBackwardUpFace(BlockGetter pLevel, BlockPos pPos)
    {
        for (int i = -1; i <= 1; ++i)
        {
            for (int j = -1; j <= 1; ++j)
            {
                BlockPos blockpos = pPos.offset(i, 0, j);
                FluidState fluidstate = pLevel.getFluidState(blockpos);

                if (!fluidstate.getType().isSame(this.getType()) && !pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public void tick(Level pLevel, BlockPos pPos)
    {
        this.getType().tick(pLevel, pPos, this);
    }

    public void animateTick(Level p_76167_, BlockPos p_76168_, Random p_76169_)
    {
        this.getType().animateTick(p_76167_, p_76168_, this, p_76169_);
    }

    public boolean isRandomlyTicking()
    {
        return this.getType().isRandomlyTicking();
    }

    public void randomTick(Level pLevel, BlockPos pPos, Random pRandom)
    {
        this.getType().randomTick(pLevel, pPos, this, pRandom);
    }

    public Vec3 getFlow(BlockGetter p_76180_, BlockPos p_76181_)
    {
        return this.getType().getFlow(p_76180_, p_76181_, this);
    }

    public BlockState createLegacyBlock()
    {
        return this.getType().createLegacyBlock(this);
    }

    @Nullable
    public ParticleOptions getDripParticle()
    {
        return this.getType().getDripParticle();
    }

    public boolean is(TagKey<Fluid> pTag)
    {
        return this.getType().builtInRegistryHolder().is(pTag);
    }

    public boolean is(HolderSet<Fluid> pTag)
    {
        return pTag.contains(this.getType().builtInRegistryHolder());
    }

    public boolean is(Fluid pTag)
    {
        return this.getType() == pTag;
    }

    public float getExplosionResistance()
    {
        return this.getType().getExplosionResistance();
    }

    public boolean canBeReplacedWith(BlockGetter p_76159_, BlockPos p_76160_, Fluid p_76161_, Direction p_76162_)
    {
        return this.getType().canBeReplacedWith(this, p_76159_, p_76160_, p_76161_, p_76162_);
    }

    public VoxelShape getShape(BlockGetter p_76184_, BlockPos p_76185_)
    {
        return this.getType().getShape(this, p_76184_, p_76185_);
    }

    public Holder<Fluid> holder()
    {
        return this.owner.builtInRegistryHolder();
    }

    public Stream<TagKey<Fluid>> getTags()
    {
        return this.owner.builtInRegistryHolder().tags();
    }
}
