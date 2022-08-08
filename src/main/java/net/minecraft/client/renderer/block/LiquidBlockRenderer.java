package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;

public class LiquidBlockRenderer
{
    private static final float MAX_FLUID_HEIGHT = 0.8888889F;
    private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private TextureAtlasSprite waterOverlay;

    protected void setupSprites()
    {
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
        this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
    }

    private static boolean isNeighborSameFluid(FluidState p_203186_, FluidState p_203187_)
    {
        return p_203187_.getType().isSame(p_203186_.getType());
    }

    private static boolean isFaceOccludedByState(BlockGetter pLevel, Direction pFace, float pHeight, BlockPos pPos, BlockState pState)
    {
        if (pState.canOcclude())
        {
            VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)pHeight, 1.0D);
            VoxelShape voxelshape1 = pState.getOcclusionShape(pLevel, pPos);
            return Shapes.blockOccudes(voxelshape, voxelshape1, pFace);
        }
        else
        {
            return false;
        }
    }

    private static boolean isFaceOccludedByNeighbor(BlockGetter p_203180_, BlockPos p_203181_, Direction p_203182_, float p_203183_, BlockState p_203184_)
    {
        return isFaceOccludedByState(p_203180_, p_203182_, p_203183_, p_203181_.relative(p_203182_), p_203184_);
    }

    private static boolean isFaceOccludedBySelf(BlockGetter pLevel, BlockPos pPos, BlockState pState, Direction pFace)
    {
        return isFaceOccludedByState(pLevel, pFace.getOpposite(), 1.0F, pPos, pState);
    }

    public static boolean shouldRenderFace(BlockAndTintGetter p_203167_, BlockPos p_203168_, FluidState p_203169_, BlockState p_203170_, Direction p_203171_, FluidState p_203172_)
    {
        return !isFaceOccludedBySelf(p_203167_, p_203168_, p_203170_, p_203171_) && !isNeighborSameFluid(p_203169_, p_203172_);
    }

    public boolean tesselate(BlockAndTintGetter p_203174_, BlockPos p_203175_, VertexConsumer p_203176_, BlockState p_203177_, FluidState p_203178_)
    {
        BlockState blockstate = p_203178_.createLegacyBlock();
        boolean flg;

        try
        {
            if (Config.isShaders())
            {
                SVertexBuilder.pushEntity(blockstate, p_203176_);
            }

            boolean flag = p_203178_.is(FluidTags.LAVA);
            TextureAtlasSprite[] atextureatlassprite = flag ? this.lavaIcons : this.waterIcons;

            if (Reflector.ForgeHooksClient_getFluidSprites.exists())
            {
                TextureAtlasSprite[] atextureatlassprite1 = (TextureAtlasSprite[])Reflector.call(Reflector.ForgeHooksClient_getFluidSprites, p_203174_, p_203175_, p_203178_);

                if (atextureatlassprite1 != null)
                {
                    atextureatlassprite = atextureatlassprite1;
                }
            }

            RenderEnv renderenv = p_203176_.getRenderEnv(blockstate, p_203175_);
            boolean flag1 = !flag && Minecraft.useAmbientOcclusion();
            int i = -1;
            float f = 1.0F;

            if (Reflector.IForgeFluid_getAttributes.exists())
            {
                Object object = Reflector.call(p_203178_.getType(), Reflector.IForgeFluid_getAttributes);

                if (object != null && Reflector.FluidAttributes_getColor.exists())
                {
                    i = Reflector.callInt(object, Reflector.FluidAttributes_getColor, p_203174_, p_203175_);
                    f = (float)(i >> 24 & 255) / 255.0F;
                }
            }

            BlockState blockstate6 = p_203174_.getBlockState(p_203175_.relative(Direction.DOWN));
            FluidState fluidstate = blockstate6.getFluidState();
            BlockState blockstate1 = p_203174_.getBlockState(p_203175_.relative(Direction.UP));
            FluidState fluidstate1 = blockstate1.getFluidState();
            BlockState blockstate2 = p_203174_.getBlockState(p_203175_.relative(Direction.NORTH));
            FluidState fluidstate2 = blockstate2.getFluidState();
            BlockState blockstate3 = p_203174_.getBlockState(p_203175_.relative(Direction.SOUTH));
            FluidState fluidstate3 = blockstate3.getFluidState();
            BlockState blockstate4 = p_203174_.getBlockState(p_203175_.relative(Direction.WEST));
            FluidState fluidstate4 = blockstate4.getFluidState();
            BlockState blockstate5 = p_203174_.getBlockState(p_203175_.relative(Direction.EAST));
            FluidState fluidstate5 = blockstate5.getFluidState();
            boolean flag2 = !isNeighborSameFluid(p_203178_, fluidstate1);
            boolean flag3 = shouldRenderFace(p_203174_, p_203175_, p_203178_, p_203177_, Direction.DOWN, fluidstate) && !isFaceOccludedByNeighbor(p_203174_, p_203175_, Direction.DOWN, 0.8888889F, blockstate6);
            boolean flag4 = shouldRenderFace(p_203174_, p_203175_, p_203178_, p_203177_, Direction.NORTH, fluidstate2);
            boolean flag5 = shouldRenderFace(p_203174_, p_203175_, p_203178_, p_203177_, Direction.SOUTH, fluidstate3);
            boolean flag6 = shouldRenderFace(p_203174_, p_203175_, p_203178_, p_203177_, Direction.WEST, fluidstate4);
            boolean flag7 = shouldRenderFace(p_203174_, p_203175_, p_203178_, p_203177_, Direction.EAST, fluidstate5);

            if (flag2 || flag3 || flag7 || flag6 || flag4 || flag5)
            {
                if (i < 0)
                {
                    i = CustomColors.getFluidColor(p_203174_, blockstate, p_203175_, renderenv);
                }

                float f23 = (float)(i >> 16 & 255) / 255.0F;
                float f1 = (float)(i >> 8 & 255) / 255.0F;
                float f2 = (float)(i & 255) / 255.0F;
                boolean flag9 = false;
                float f3 = p_203174_.getShade(Direction.DOWN, true);
                float f4 = p_203174_.getShade(Direction.UP, true);
                float f5 = p_203174_.getShade(Direction.NORTH, true);
                float f6 = p_203174_.getShade(Direction.WEST, true);
                Fluid fluid = p_203178_.getType();
                float f7 = this.getHeight(p_203174_, fluid, p_203175_, p_203177_, p_203178_);
                float f8;
                float f9;
                float f10;
                float f11;

                if (f7 >= 1.0F)
                {
                    f8 = 1.0F;
                    f9 = 1.0F;
                    f10 = 1.0F;
                    f11 = 1.0F;
                }
                else
                {
                    float f12 = this.getHeight(p_203174_, fluid, p_203175_.north(), blockstate2, fluidstate2);
                    float f13 = this.getHeight(p_203174_, fluid, p_203175_.south(), blockstate3, fluidstate3);
                    float f14 = this.getHeight(p_203174_, fluid, p_203175_.east(), blockstate5, fluidstate5);
                    float f15 = this.getHeight(p_203174_, fluid, p_203175_.west(), blockstate4, fluidstate4);
                    f8 = this.calculateAverageHeight(p_203174_, fluid, f7, f12, f14, p_203175_.relative(Direction.NORTH).relative(Direction.EAST));
                    f9 = this.calculateAverageHeight(p_203174_, fluid, f7, f12, f15, p_203175_.relative(Direction.NORTH).relative(Direction.WEST));
                    f10 = this.calculateAverageHeight(p_203174_, fluid, f7, f13, f14, p_203175_.relative(Direction.SOUTH).relative(Direction.EAST));
                    f11 = this.calculateAverageHeight(p_203174_, fluid, f7, f13, f15, p_203175_.relative(Direction.SOUTH).relative(Direction.WEST));
                }

                double d1 = (double)(p_203175_.getX() & 15);
                double d2 = (double)(p_203175_.getY() & 15);
                double d0 = (double)(p_203175_.getZ() & 15);

                if (Config.isRenderRegions())
                {
                    int j = p_203175_.getX() >> 4 << 4;
                    int k = p_203175_.getY() >> 4 << 4;
                    int l = p_203175_.getZ() >> 4 << 4;
                    int i1 = 8;
                    int j1 = j >> i1 << i1;
                    int k1 = l >> i1 << i1;
                    int l1 = j - j1;
                    int i2 = l - k1;
                    d1 += (double)l1;
                    d2 += (double)k;
                    d0 += (double)i2;
                }

                if (Config.isShaders() && Shaders.useMidBlockAttrib)
                {
                    p_203176_.setMidBlock((float)(d1 + 0.5D), (float)(d2 + 0.5D), (float)(d0 + 0.5D));
                }

                float f24 = 0.001F;
                float f25 = flag3 ? 0.001F : 0.0F;

                if (flag2 && !isFaceOccludedByNeighbor(p_203174_, p_203175_, Direction.UP, Math.min(Math.min(f9, f11), Math.min(f10, f8)), blockstate1))
                {
                    flag9 = true;
                    f9 -= 0.001F;
                    f11 -= 0.001F;
                    f10 -= 0.001F;
                    f8 -= 0.001F;
                    Vec3 vec3 = p_203178_.getFlow(p_203174_, p_203175_);
                    float f16;
                    float f17;
                    float f18;
                    float f27;
                    float f29;
                    float f31;
                    float f34;
                    float f37;

                    if (vec3.x == 0.0D && vec3.z == 0.0D)
                    {
                        TextureAtlasSprite textureatlassprite2 = atextureatlassprite[0];
                        p_203176_.setSprite(textureatlassprite2);
                        f27 = textureatlassprite2.getU(0.0D);
                        f16 = textureatlassprite2.getV(0.0D);
                        f29 = f27;
                        f37 = textureatlassprite2.getV(16.0D);
                        f31 = textureatlassprite2.getU(16.0D);
                        f17 = f37;
                        f34 = f31;
                        f18 = f16;
                    }
                    else
                    {
                        TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
                        p_203176_.setSprite(textureatlassprite);
                        float f19 = (float)Mth.atan2(vec3.z, vec3.x) - ((float)Math.PI / 2F);
                        float f20 = Mth.sin(f19) * 0.25F;
                        float f21 = Mth.cos(f19) * 0.25F;
                        float f22 = 8.0F;
                        f27 = textureatlassprite.getU((double)(8.0F + (-f21 - f20) * 16.0F));
                        f16 = textureatlassprite.getV((double)(8.0F + (-f21 + f20) * 16.0F));
                        f29 = textureatlassprite.getU((double)(8.0F + (-f21 + f20) * 16.0F));
                        f37 = textureatlassprite.getV((double)(8.0F + (f21 + f20) * 16.0F));
                        f31 = textureatlassprite.getU((double)(8.0F + (f21 + f20) * 16.0F));
                        f17 = textureatlassprite.getV((double)(8.0F + (f21 - f20) * 16.0F));
                        f34 = textureatlassprite.getU((double)(8.0F + (f21 - f20) * 16.0F));
                        f18 = textureatlassprite.getV((double)(8.0F + (-f21 - f20) * 16.0F));
                    }

                    float f41 = (f27 + f29 + f31 + f34) / 4.0F;
                    float f42 = (f16 + f37 + f17 + f18) / 4.0F;
                    float f43 = (float)atextureatlassprite[0].getWidth() / (atextureatlassprite[0].getU1() - atextureatlassprite[0].getU0());
                    float f44 = (float)atextureatlassprite[0].getHeight() / (atextureatlassprite[0].getV1() - atextureatlassprite[0].getV0());
                    float f45 = 4.0F / Math.max(f44, f43);
                    f27 = Mth.lerp(f45, f27, f41);
                    f29 = Mth.lerp(f45, f29, f41);
                    f31 = Mth.lerp(f45, f31, f41);
                    f34 = Mth.lerp(f45, f34, f41);
                    f16 = Mth.lerp(f45, f16, f42);
                    f37 = Mth.lerp(f45, f37, f42);
                    f17 = Mth.lerp(f45, f17, f42);
                    f18 = Mth.lerp(f45, f18, f42);
                    int j2 = this.getLightColor(p_203174_, p_203175_);
                    int k2 = j2;
                    int l2 = j2;
                    int i3 = j2;
                    int j3 = j2;

                    if (flag1)
                    {
                        BlockPos blockpos = p_203175_.north();
                        BlockPos blockpos1 = p_203175_.south();
                        BlockPos blockpos2 = p_203175_.east();
                        BlockPos blockpos3 = p_203175_.west();
                        int k3 = this.getLightColor(p_203174_, blockpos);
                        int l3 = this.getLightColor(p_203174_, blockpos1);
                        int i4 = this.getLightColor(p_203174_, blockpos2);
                        int j4 = this.getLightColor(p_203174_, blockpos3);
                        int k4 = this.getLightColor(p_203174_, blockpos.west());
                        int l4 = this.getLightColor(p_203174_, blockpos1.west());
                        int i5 = this.getLightColor(p_203174_, blockpos1.east());
                        int j5 = this.getLightColor(p_203174_, blockpos.east());
                        k2 = ModelBlockRenderer.AmbientOcclusionFace.blend(k3, k4, j4, j2);
                        l2 = ModelBlockRenderer.AmbientOcclusionFace.blend(l3, l4, j4, j2);
                        i3 = ModelBlockRenderer.AmbientOcclusionFace.blend(l3, i5, i4, j2);
                        j3 = ModelBlockRenderer.AmbientOcclusionFace.blend(k3, j5, i4, j2);
                    }

                    float f49 = f4 * f23;
                    float f51 = f4 * f1;
                    float f53 = f4 * f2;
                    this.vertexVanilla(p_203176_, d1 + 0.0D, d2 + (double)f9, d0 + 0.0D, f49, f51, f53, f, f27, f16, k2);
                    this.vertexVanilla(p_203176_, d1 + 0.0D, d2 + (double)f11, d0 + 1.0D, f49, f51, f53, f, f29, f37, l2);
                    this.vertexVanilla(p_203176_, d1 + 1.0D, d2 + (double)f10, d0 + 1.0D, f49, f51, f53, f, f31, f17, i3);
                    this.vertexVanilla(p_203176_, d1 + 1.0D, d2 + (double)f8, d0 + 0.0D, f49, f51, f53, f, f34, f18, j3);

                    if (p_203178_.shouldRenderBackwardUpFace(p_203174_, p_203175_.above()))
                    {
                        this.vertexVanilla(p_203176_, d1 + 0.0D, d2 + (double)f9, d0 + 0.0D, f49, f51, f53, f, f27, f16, k2);
                        this.vertexVanilla(p_203176_, d1 + 1.0D, d2 + (double)f8, d0 + 0.0D, f49, f51, f53, f, f34, f18, j3);
                        this.vertexVanilla(p_203176_, d1 + 1.0D, d2 + (double)f10, d0 + 1.0D, f49, f51, f53, f, f31, f17, i3);
                        this.vertexVanilla(p_203176_, d1 + 0.0D, d2 + (double)f11, d0 + 1.0D, f49, f51, f53, f, f29, f37, l2);
                    }
                }

                if (flag3)
                {
                    p_203176_.setSprite(atextureatlassprite[0]);
                    float f26 = atextureatlassprite[0].getU0();
                    float f28 = atextureatlassprite[0].getU1();
                    float f30 = atextureatlassprite[0].getV0();
                    float f32 = atextureatlassprite[0].getV1();
                    int l5 = this.getLightColor(p_203174_, p_203175_.below());
                    float f36 = p_203174_.getShade(Direction.DOWN, true);
                    float f38 = f36 * f23;
                    float f39 = f36 * f1;
                    float f40 = f36 * f2;
                    this.vertexVanilla(p_203176_, d1, d2 + (double)f25, d0 + 1.0D, f38, f39, f40, f, f26, f32, l5);
                    this.vertexVanilla(p_203176_, d1, d2 + (double)f25, d0, f38, f39, f40, f, f26, f30, l5);
                    this.vertexVanilla(p_203176_, d1 + 1.0D, d2 + (double)f25, d0, f38, f39, f40, f, f28, f30, l5);
                    this.vertexVanilla(p_203176_, d1 + 1.0D, d2 + (double)f25, d0 + 1.0D, f38, f39, f40, f, f28, f32, l5);
                    flag9 = true;
                }

                int k5 = this.getLightColor(p_203174_, p_203175_);

                for (Direction direction : Direction.Plane.HORIZONTAL)
                {
                    float f33;
                    float f35;
                    double d3;
                    double d4;
                    double d5;
                    double d6;
                    boolean flag10;

                    switch (direction)
                    {
                        case NORTH:
                            f33 = f9;
                            f35 = f8;
                            d3 = d1;
                            d5 = d1 + 1.0D;
                            d4 = d0 + (double)0.001F;
                            d6 = d0 + (double)0.001F;
                            flag10 = flag4;
                            break;

                        case SOUTH:
                            f33 = f10;
                            f35 = f11;
                            d3 = d1 + 1.0D;
                            d5 = d1;
                            d4 = d0 + 1.0D - (double)0.001F;
                            d6 = d0 + 1.0D - (double)0.001F;
                            flag10 = flag5;
                            break;

                        case WEST:
                            f33 = f11;
                            f35 = f9;
                            d3 = d1 + (double)0.001F;
                            d5 = d1 + (double)0.001F;
                            d4 = d0 + 1.0D;
                            d6 = d0;
                            flag10 = flag6;
                            break;

                        default:
                            f33 = f8;
                            f35 = f10;
                            d3 = d1 + 1.0D - (double)0.001F;
                            d5 = d1 + 1.0D - (double)0.001F;
                            d4 = d0;
                            d6 = d0 + 1.0D;
                            flag10 = flag7;
                    }

                    if (flag10 && !isFaceOccludedByNeighbor(p_203174_, p_203175_, direction, Math.max(f33, f35), p_203174_.getBlockState(p_203175_.relative(direction))))
                    {
                        flag9 = true;
                        BlockPos blockpos4 = p_203175_.relative(direction);
                        TextureAtlasSprite textureatlassprite1 = atextureatlassprite[1];
                        float f46 = 0.0F;
                        float f47 = 0.0F;
                        boolean flag11 = !flag;

                        if (Reflector.IForgeBlockState_shouldDisplayFluidOverlay.exists())
                        {
                            flag11 = atextureatlassprite[2] != null;
                        }

                        if (flag11)
                        {
                            BlockState blockstate7 = p_203174_.getBlockState(blockpos4);
                            Block block = blockstate7.getBlock();
                            boolean flag12 = false;

                            if (Reflector.IForgeBlockState_shouldDisplayFluidOverlay.exists())
                            {
                                flag12 = Reflector.callBoolean(blockstate7, Reflector.IForgeBlockState_shouldDisplayFluidOverlay, p_203174_, blockpos4, p_203178_);
                            }

                            if (flag12 || block instanceof HalfTransparentBlock || block instanceof LeavesBlock || block == Blocks.BEACON)
                            {
                                textureatlassprite1 = this.waterOverlay;
                            }

                            if (block == Blocks.FARMLAND || block == Blocks.DIRT_PATH)
                            {
                                f46 = 0.9375F;
                                f47 = 0.9375F;
                            }

                            if (block instanceof SlabBlock)
                            {
                                SlabBlock slabblock = (SlabBlock)block;

                                if (blockstate7.getValue(SlabBlock.TYPE) == SlabType.BOTTOM)
                                {
                                    f46 = 0.5F;
                                    f47 = 0.5F;
                                }
                            }
                        }

                        p_203176_.setSprite(textureatlassprite1);

                        if (!(f33 <= f46) || !(f35 <= f47))
                        {
                            f46 = Math.min(f46, f33);
                            f47 = Math.min(f47, f35);

                            if (f46 > f24)
                            {
                                f46 -= f24;
                            }

                            if (f47 > f24)
                            {
                                f47 -= f24;
                            }

                            float f48 = textureatlassprite1.getV((double)((1.0F - f46) * 16.0F * 0.5F));
                            float f50 = textureatlassprite1.getV((double)((1.0F - f47) * 16.0F * 0.5F));
                            float f52 = textureatlassprite1.getU(0.0D);
                            float f54 = textureatlassprite1.getU(8.0D);
                            float f55 = textureatlassprite1.getV((double)((1.0F - f33) * 16.0F * 0.5F));
                            float f56 = textureatlassprite1.getV((double)((1.0F - f35) * 16.0F * 0.5F));
                            float f57 = textureatlassprite1.getV(8.0D);
                            float f58 = direction != Direction.NORTH && direction != Direction.SOUTH ? p_203174_.getShade(Direction.WEST, true) : p_203174_.getShade(Direction.NORTH, true);
                            float f59 = f4 * f58 * f23;
                            float f60 = f4 * f58 * f1;
                            float f61 = f4 * f58 * f2;
                            this.vertexVanilla(p_203176_, d3, d2 + (double)f33, d4, f59, f60, f61, f, f52, f55, k5);
                            this.vertexVanilla(p_203176_, d5, d2 + (double)f35, d6, f59, f60, f61, f, f54, f56, k5);
                            this.vertexVanilla(p_203176_, d5, d2 + (double)f25, d6, f59, f60, f61, f, f54, f50, k5);
                            this.vertexVanilla(p_203176_, d3, d2 + (double)f25, d4, f59, f60, f61, f, f52, f48, k5);

                            if (textureatlassprite1 != this.waterOverlay)
                            {
                                this.vertexVanilla(p_203176_, d3, d2 + (double)f25, d4, f59, f60, f61, f, f52, f48, k5);
                                this.vertexVanilla(p_203176_, d5, d2 + (double)f25, d6, f59, f60, f61, f, f54, f50, k5);
                                this.vertexVanilla(p_203176_, d5, d2 + (double)f35, d6, f59, f60, f61, f, f54, f56, k5);
                                this.vertexVanilla(p_203176_, d3, d2 + (double)f33, d4, f59, f60, f61, f, f52, f55, k5);
                            }
                        }
                    }
                }

                p_203176_.setSprite((TextureAtlasSprite)null);
                return flag9;
            }

            flg = flag7;
        }
        finally
        {
            if (Config.isShaders())
            {
                SVertexBuilder.popEntity(p_203176_);
            }
        }

        return flg;
    }

    private float calculateAverageHeight(BlockAndTintGetter p_203150_, Fluid p_203151_, float p_203152_, float p_203153_, float p_203154_, BlockPos p_203155_)
    {
        if (!(p_203154_ >= 1.0F) && !(p_203153_ >= 1.0F))
        {
            float[] afloat = new float[2];

            if (p_203154_ > 0.0F || p_203153_ > 0.0F)
            {
                float f = this.getHeight(p_203150_, p_203151_, p_203155_);

                if (f >= 1.0F)
                {
                    return 1.0F;
                }

                this.a(afloat, f);
            }

            this.a(afloat, p_203152_);
            this.a(afloat, p_203154_);
            this.a(afloat, p_203153_);
            return afloat[0] / afloat[1];
        }
        else
        {
            return 1.0F;
        }
    }

    private void a(float[] p_203189_, float p_203190_)
    {
        if (p_203190_ >= 0.8F)
        {
            p_203189_[0] += p_203190_ * 10.0F;
            p_203189_[1] += 10.0F;
        }
        else if (p_203190_ >= 0.0F)
        {
            p_203189_[0] += p_203190_;
            p_203189_[1] += 1.0F;
        }
    }

    private float getHeight(BlockAndTintGetter p_203157_, Fluid p_203158_, BlockPos p_203159_)
    {
        BlockState blockstate = p_203157_.getBlockState(p_203159_);
        return this.getHeight(p_203157_, p_203158_, p_203159_, blockstate, blockstate.getFluidState());
    }

    private float getHeight(BlockAndTintGetter p_203161_, Fluid p_203162_, BlockPos p_203163_, BlockState p_203164_, FluidState p_203165_)
    {
        if (p_203162_.isSame(p_203165_.getType()))
        {
            BlockState blockstate = p_203161_.getBlockState(p_203163_.above());
            return p_203162_.isSame(blockstate.getFluidState().getType()) ? 1.0F : p_203165_.getOwnHeight();
        }
        else
        {
            return !p_203164_.getMaterial().isSolid() ? 0.0F : -1.0F;
        }
    }

    private void vertex(VertexConsumer pConsumer, double pX, double p_110987_, double pY, float p_110989_, float pZ, float p_110991_, float pRed, float pGreen, int pBlue)
    {
        pConsumer.vertex(pX, p_110987_, pY).color(p_110989_, pZ, p_110991_, 1.0F).uv(pRed, pGreen).uv2(pBlue).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private void vertexVanilla(VertexConsumer buffer, double x, double y, double z, float red, float green, float blue, float alpha, float u, float v, int combinedLight)
    {
        buffer.vertex(x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(combinedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private int getLightColor(BlockAndTintGetter pLevel, BlockPos pPos)
    {
        int i = LevelRenderer.getLightColor(pLevel, pPos);
        int j = LevelRenderer.getLightColor(pLevel, pPos.above());
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }
}
