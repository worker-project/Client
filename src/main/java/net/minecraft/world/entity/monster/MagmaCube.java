package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class MagmaCube extends Slime
{
    public MagmaCube(EntityType <? extends MagmaCube > p_32968_, Level p_32969_)
    {
        super(p_32968_, p_32969_);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.2F);
    }

    public static boolean checkMagmaCubeSpawnRules(EntityType<MagmaCube> p_32981_, LevelAccessor p_32982_, MobSpawnType p_32983_, BlockPos p_32984_, Random p_32985_)
    {
        return p_32982_.getDifficulty() != Difficulty.PEACEFUL;
    }

    public boolean checkSpawnObstruction(LevelReader pLevel)
    {
        return pLevel.isUnobstructed(this) && !pLevel.containsAnyLiquid(this.getBoundingBox());
    }

    protected void setSize(int pSize, boolean pResetHealth)
    {
        super.setSize(pSize, pResetHealth);
        this.getAttribute(Attributes.ARMOR).setBaseValue((double)(pSize * 3));
    }

    public float getBrightness()
    {
        return 1.0F;
    }

    protected ParticleOptions getParticleType()
    {
        return ParticleTypes.FLAME;
    }

    protected ResourceLocation getDefaultLootTable()
    {
        return this.isTiny() ? BuiltInLootTables.EMPTY : this.getType().getDefaultLootTable();
    }

    public boolean isOnFire()
    {
        return false;
    }

    protected int getJumpDelay()
    {
        return super.getJumpDelay() * 4;
    }

    protected void decreaseSquish()
    {
        this.targetSquish *= 0.9F;
    }

    protected void jumpFromGround()
    {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, (double)(this.getJumpPower() + (float)this.getSize() * 0.1F), vec3.z);
        this.hasImpulse = true;
    }

    protected void jumpInLiquid(TagKey<Fluid> pFluidTag)
    {
        if (pFluidTag == FluidTags.LAVA)
        {
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.x, (double)(0.22F + (float)this.getSize() * 0.05F), vec3.z);
            this.hasImpulse = true;
        }
        else
        {
            super.jumpInLiquid(pFluidTag);
        }
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource)
    {
        return false;
    }

    protected boolean isDealsDamage()
    {
        return this.isEffectiveAi();
    }

    protected float getAttackDamage()
    {
        return super.getAttackDamage() + 2.0F;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource)
    {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_HURT_SMALL : SoundEvents.MAGMA_CUBE_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_DEATH_SMALL : SoundEvents.MAGMA_CUBE_DEATH;
    }

    protected SoundEvent getSquishSound()
    {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_SQUISH_SMALL : SoundEvents.MAGMA_CUBE_SQUISH;
    }

    protected SoundEvent getJumpSound()
    {
        return SoundEvents.MAGMA_CUBE_JUMP;
    }
}
