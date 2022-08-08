package net.minecraft.client.renderer;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;
import net.optifine.render.GLConst;
import net.optifine.shaders.Shaders;

public class FogRenderer
{
    private static final int WATER_FOG_DISTANCE = 96;
    public static final float BIOME_FOG_TRANSITION_TIME = 5000.0F;
    public static float fogRed;
    public static float fogGreen;
    public static float fogBlue;
    private static int targetBiomeFog = -1;
    private static int previousBiomeFog = -1;
    private static long biomeChangedTime = -1L;
    public static boolean fogStandard = false;

    public static void setupColor(Camera pActiveRenderInfo, float pPartialTicks, ClientLevel pLevel, int pRenderDistanceChunks, float pBossColorModifier)
    {
        FogType fogtype = pActiveRenderInfo.getFluidInCamera();
        Entity entity = pActiveRenderInfo.getEntity();

        if (fogtype == FogType.WATER)
        {
            long i = Util.getMillis();
            int j = pLevel.getBiome(new BlockPos(pActiveRenderInfo.getPosition())).value().getWaterFogColor();

            if (biomeChangedTime < 0L)
            {
                targetBiomeFog = j;
                previousBiomeFog = j;
                biomeChangedTime = i;
            }

            int k = targetBiomeFog >> 16 & 255;
            int l = targetBiomeFog >> 8 & 255;
            int i1 = targetBiomeFog & 255;
            int j1 = previousBiomeFog >> 16 & 255;
            int k1 = previousBiomeFog >> 8 & 255;
            int l1 = previousBiomeFog & 255;
            float f = Mth.clamp((float)(i - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
            float f1 = Mth.lerp(f, (float)j1, (float)k);
            float f2 = Mth.lerp(f, (float)k1, (float)l);
            float f3 = Mth.lerp(f, (float)l1, (float)i1);
            fogRed = f1 / 255.0F;
            fogGreen = f2 / 255.0F;
            fogBlue = f3 / 255.0F;

            if (targetBiomeFog != j)
            {
                targetBiomeFog = j;
                previousBiomeFog = Mth.floor(f1) << 16 | Mth.floor(f2) << 8 | Mth.floor(f3);
                biomeChangedTime = i;
            }
        }
        else if (fogtype == FogType.LAVA)
        {
            fogRed = 0.6F;
            fogGreen = 0.1F;
            fogBlue = 0.0F;
            biomeChangedTime = -1L;
        }
        else if (fogtype == FogType.POWDER_SNOW)
        {
            fogRed = 0.623F;
            fogGreen = 0.734F;
            fogBlue = 0.785F;
            biomeChangedTime = -1L;
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
        }
        else
        {
            float f4 = 0.25F + 0.75F * (float)pRenderDistanceChunks / 32.0F;
            f4 = 1.0F - (float)Math.pow((double)f4, 0.25D);
            Vec3 vec3 = pLevel.getSkyColor(pActiveRenderInfo.getPosition(), pPartialTicks);
            vec3 = CustomColors.getWorldSkyColor(vec3, pLevel, pActiveRenderInfo.getEntity(), pPartialTicks);
            float f7 = (float)vec3.x;
            float f9 = (float)vec3.y;
            float f10 = (float)vec3.z;
            float f11 = Mth.clamp(Mth.cos(pLevel.getTimeOfDay(pPartialTicks) * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
            BiomeManager biomemanager = pLevel.getBiomeManager();
            Vec3 vec33 = pActiveRenderInfo.getPosition().subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
            Vec3 vec34 = CubicSampler.gaussianSampleVec3(vec33, (xIn, yIn, zIn) ->
            {
                return pLevel.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomemanager.getNoiseBiomeAtQuart(xIn, yIn, zIn).value().getFogColor()), f11);
            });
            vec34 = CustomColors.getWorldFogColor(vec34, pLevel, pActiveRenderInfo.getEntity(), pPartialTicks);
            fogRed = (float)vec34.x();
            fogGreen = (float)vec34.y();
            fogBlue = (float)vec34.z();

            if (pRenderDistanceChunks >= 4)
            {
                float f12 = Mth.sin(pLevel.getSunAngle(pPartialTicks)) > 0.0F ? -1.0F : 1.0F;
                Vector3f vector3f = new Vector3f(f12, 0.0F, 0.0F);
                float f16 = pActiveRenderInfo.getLookVector().dot(vector3f);

                if (f16 < 0.0F)
                {
                    f16 = 0.0F;
                }

                if (f16 > 0.0F)
                {
                    float[] afloat = pLevel.effects().getSunriseColor(pLevel.getTimeOfDay(pPartialTicks), pPartialTicks);

                    if (afloat != null)
                    {
                        f16 *= afloat[3];
                        fogRed = fogRed * (1.0F - f16) + afloat[0] * f16;
                        fogGreen = fogGreen * (1.0F - f16) + afloat[1] * f16;
                        fogBlue = fogBlue * (1.0F - f16) + afloat[2] * f16;
                    }
                }
            }

            fogRed += (f7 - fogRed) * f4;
            fogGreen += (f9 - fogGreen) * f4;
            fogBlue += (f10 - fogBlue) * f4;
            float f13 = pLevel.getRainLevel(pPartialTicks);

            if (f13 > 0.0F)
            {
                float f14 = 1.0F - f13 * 0.5F;
                float f17 = 1.0F - f13 * 0.4F;
                fogRed *= f14;
                fogGreen *= f14;
                fogBlue *= f17;
            }

            float f15 = pLevel.getThunderLevel(pPartialTicks);

            if (f15 > 0.0F)
            {
                float f18 = 1.0F - f15 * 0.5F;
                fogRed *= f18;
                fogGreen *= f18;
                fogBlue *= f18;
            }

            biomeChangedTime = -1L;
        }

        float f5 = ((float)pActiveRenderInfo.getPosition().y - (float)pLevel.getMinBuildHeight()) * pLevel.getLevelData().getClearColorScale();

        if (pActiveRenderInfo.getEntity() instanceof LivingEntity && ((LivingEntity)pActiveRenderInfo.getEntity()).hasEffect(MobEffects.BLINDNESS))
        {
            int i2 = ((LivingEntity)pActiveRenderInfo.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();

            if (i2 < 20)
            {
                f5 = 1.0F - (float)i2 / 20.0F;
            }
            else
            {
                f5 = 0.0F;
            }
        }

        if (f5 < 1.0F && fogtype != FogType.LAVA && fogtype != FogType.POWDER_SNOW)
        {
            if (f5 < 0.0F)
            {
                f5 = 0.0F;
            }

            f5 *= f5;
            fogRed *= f5;
            fogGreen *= f5;
            fogBlue *= f5;
        }

        if (pBossColorModifier > 0.0F)
        {
            fogRed = fogRed * (1.0F - pBossColorModifier) + fogRed * 0.7F * pBossColorModifier;
            fogGreen = fogGreen * (1.0F - pBossColorModifier) + fogGreen * 0.6F * pBossColorModifier;
            fogBlue = fogBlue * (1.0F - pBossColorModifier) + fogBlue * 0.6F * pBossColorModifier;
        }

        float f6;

        if (fogtype == FogType.WATER)
        {
            if (entity instanceof LocalPlayer)
            {
                f6 = ((LocalPlayer)entity).getWaterVision();
            }
            else
            {
                f6 = 1.0F;
            }
        }
        else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.NIGHT_VISION))
        {
            f6 = GameRenderer.getNightVisionScale((LivingEntity)entity, pPartialTicks);
        }
        else
        {
            f6 = 0.0F;
        }

        if (fogRed != 0.0F && fogGreen != 0.0F && fogBlue != 0.0F)
        {
            float f8 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            fogRed = fogRed * (1.0F - f6) + fogRed * f8 * f6;
            fogGreen = fogGreen * (1.0F - f6) + fogGreen * f8 * f6;
            fogBlue = fogBlue * (1.0F - f6) + fogBlue * f8 * f6;
        }

        if (fogtype == FogType.WATER)
        {
            Entity entity1 = pActiveRenderInfo.getEntity();
            Vec3 vec31 = CustomColors.getUnderwaterColor(pLevel, entity1.getX(), entity1.getY() + 1.0D, entity1.getZ());

            if (vec31 != null)
            {
                fogRed = (float)vec31.x;
                fogGreen = (float)vec31.y;
                fogBlue = (float)vec31.z;
            }
        }
        else if (fogtype == FogType.LAVA)
        {
            Entity entity2 = pActiveRenderInfo.getEntity();
            Vec3 vec32 = CustomColors.getUnderlavaColor(pLevel, entity2.getX(), entity2.getY() + 1.0D, entity2.getZ());

            if (vec32 != null)
            {
                fogRed = (float)vec32.x;
                fogGreen = (float)vec32.y;
                fogBlue = (float)vec32.z;
            }
        }

        if (Reflector.EntityViewRenderEvent_FogColors_Constructor.exists())
        {
            Object object = Reflector.newInstance(Reflector.EntityViewRenderEvent_FogColors_Constructor, pActiveRenderInfo, pPartialTicks, fogRed, fogGreen, fogBlue);
            Reflector.postForgeBusEvent(object);
            fogRed = Reflector.callFloat(object, Reflector.EntityViewRenderEvent_FogColors_getRed);
            fogGreen = Reflector.callFloat(object, Reflector.EntityViewRenderEvent_FogColors_getGreen);
            fogBlue = Reflector.callFloat(object, Reflector.EntityViewRenderEvent_FogColors_getBlue);
        }

        Shaders.setClearColor(fogRed, fogGreen, fogBlue, 0.0F);
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }

    public static void setupNoFog()
    {
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);

        if (Config.isShaders())
        {
            Shaders.setFogDensity(0.0F);
            Shaders.setFogMode(GLConst.GL_EXP2);
            Shaders.setFogStart(1.7014117E38F);
            Shaders.setFogEnd(Float.MAX_VALUE);
        }
    }

    public static void setupFog(Camera pActiveRenderInfo, FogRenderer.FogMode pFogType, float pFarPlaneDistance, boolean pNearFog)
    {
        setupFog(pActiveRenderInfo, pFogType, pFarPlaneDistance, pNearFog, 0.0F);
    }

    public static void setupFog(Camera activeRenderInfoIn, FogRenderer.FogMode fogTypeIn, float farPlaneDistance, boolean nearFog, float partialTicks)
    {
        fogStandard = false;
        FogType fogtype = activeRenderInfoIn.getFluidInCamera();
        Entity entity = activeRenderInfoIn.getEntity();
        FogShape fogshape = FogShape.SPHERE;
        float f2 = -1.0F;

        if (Reflector.ForgeHooksClient_getFogDensity.exists())
        {
            f2 = Reflector.callFloat(Reflector.ForgeHooksClient_getFogDensity, fogTypeIn, activeRenderInfoIn, partialTicks, 0.1F);
        }

        float f;
        float f1;

        if (f2 >= 0.0F)
        {
            RenderSystem.setShaderFogStart(-8.0F);
            RenderSystem.setShaderFogEnd(f2 * 0.5F);
            f = -8.0F;
            f1 = f2 * 0.5F;

            if (Config.isShaders())
            {
                Shaders.setFogDensity(f2);
            }
        }
        else if (fogtype == FogType.LAVA)
        {
            if (entity.isSpectator())
            {
                f = -8.0F;
                f1 = farPlaneDistance * 0.5F;
            }
            else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE))
            {
                f = 0.0F;
                f1 = 3.0F;
            }
            else
            {
                f = 0.25F;
                f1 = 1.0F;
            }
        }
        else if (fogtype == FogType.POWDER_SNOW)
        {
            if (entity.isSpectator())
            {
                f = -8.0F;
                f1 = farPlaneDistance * 0.5F;
            }
            else
            {
                f = 0.0F;
                f1 = 2.0F;
            }
        }
        else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.BLINDNESS))
        {
            int i = ((LivingEntity)entity).getEffect(MobEffects.BLINDNESS).getDuration();
            float f4 = Mth.lerp(Math.min(1.0F, (float)i / 20.0F), farPlaneDistance, 5.0F);

            if (fogTypeIn == FogRenderer.FogMode.FOG_SKY)
            {
                f = 0.0F;
                f1 = f4 * 0.8F;
            }
            else
            {
                f = fogtype == FogType.WATER ? -4.0F : f4 * 0.25F;
                f1 = f4;
            }
        }
        else if (fogtype == FogType.WATER)
        {
            f = -8.0F;
            f1 = 96.0F;

            if (entity instanceof LocalPlayer)
            {
                LocalPlayer localplayer = (LocalPlayer)entity;
                f1 *= Math.max(0.25F, localplayer.getWaterVision());
                Holder<Biome> holder = localplayer.level.getBiome(localplayer.blockPosition());

                if (Biome.getBiomeCategory(holder) == Biome.BiomeCategory.SWAMP)
                {
                    f1 *= 0.85F;
                }
            }

            if (f1 > farPlaneDistance)
            {
                f1 = farPlaneDistance;
                fogshape = FogShape.CYLINDER;
            }
        }
        else if (nearFog)
        {
            fogStandard = true;
            f = farPlaneDistance * 0.05F;
            f1 = Math.min(farPlaneDistance, 192.0F) * 0.5F;
        }
        else if (fogTypeIn == FogRenderer.FogMode.FOG_SKY)
        {
            f = 0.0F;
            f1 = Math.min(farPlaneDistance, 512.0F);
            fogshape = FogShape.CYLINDER;
        }
        else
        {
            fogStandard = true;
            float f3 = Mth.clamp(farPlaneDistance / 10.0F, 4.0F, 64.0F);
            f = farPlaneDistance * Config.getFogStart();
            f1 = farPlaneDistance;
            fogshape = FogShape.CYLINDER;
        }

        RenderSystem.setShaderFogStart(f);
        RenderSystem.setShaderFogEnd(f1);
        RenderSystem.setShaderFogShape(fogshape);

        if (Config.isShaders())
        {
            Shaders.setFogStart(f);
            Shaders.setFogEnd(f1);
            Shaders.setFogMode(9729);
            Shaders.setFogShape(fogshape.ordinal());
        }

        if (Reflector.ForgeHooksClient_onFogRender.exists())
        {
            Reflector.callVoid(Reflector.ForgeHooksClient_onFogRender, fogTypeIn, activeRenderInfoIn, partialTicks, f, f1, fogshape);
        }
    }

    public static void levelFogColor()
    {
        RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);

        if (Config.isShaders())
        {
            Shaders.setFogColor(fogRed, fogGreen, fogBlue);
        }
    }

    public static enum FogMode
    {
        FOG_SKY,
        FOG_TERRAIN;
    }
}
