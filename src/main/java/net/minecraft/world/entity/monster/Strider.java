package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class Strider extends Animal implements ItemSteerable, Saddleable
{
    private static final float SUFFOCATE_STEERING_MODIFIER = 0.23F;
    private static final float SUFFOCATE_SPEED_MODIFIER = 0.66F;
    private static final float STEERING_MODIFIER = 0.55F;
    private static final Ingredient FOOD_ITEMS = Ingredient.a(Items.WARPED_FUNGUS);
    private static final Ingredient TEMPT_ITEMS = Ingredient.a(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
    @Nullable
    private TemptGoal temptGoal;
    @Nullable
    private PanicGoal panicGoal;

    public Strider(EntityType <? extends Strider > p_33862_, Level p_33863_)
    {
        super(p_33862_, p_33863_);
        this.blocksBuilding = true;
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    public static boolean checkStriderSpawnRules(EntityType<Strider> p_33922_, LevelAccessor p_33923_, MobSpawnType p_33924_, BlockPos p_33925_, Random p_33926_)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = p_33925_.mutable();

        do
        {
            blockpos$mutableblockpos.move(Direction.UP);
        }
        while (p_33923_.getFluidState(blockpos$mutableblockpos).is(FluidTags.LAVA));

        return p_33923_.getBlockState(blockpos$mutableblockpos).isAir();
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey)
    {
        if (DATA_BOOST_TIME.equals(pKey) && this.level.isClientSide)
        {
            this.steering.onSynced();
        }

        super.onSyncedDataUpdated(pKey);
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_BOOST_TIME, 0);
        this.entityData.define(DATA_SUFFOCATING, false);
        this.entityData.define(DATA_SADDLE_ID, false);
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        this.steering.addAdditionalSaveData(pCompound);
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        this.steering.readAdditionalSaveData(pCompound);
    }

    public boolean isSaddled()
    {
        return this.steering.hasSaddle();
    }

    public boolean isSaddleable()
    {
        return this.isAlive() && !this.isBaby();
    }

    public void equipSaddle(@Nullable SoundSource p_33878_)
    {
        this.steering.setSaddle(true);

        if (p_33878_ != null)
        {
            this.level.playSound((Player)null, this, SoundEvents.STRIDER_SADDLE, p_33878_, 0.5F, 1.0F);
        }
    }

    protected void registerGoals()
    {
        this.panicGoal = new PanicGoal(this, 1.65D);
        this.goalSelector.addGoal(1, this.panicGoal);
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.temptGoal = new TemptGoal(this, 1.4D, TEMPT_ITEMS, false);
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(4, new Strider.StriderGoToLavaGoal(this, 1.5D));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D, 60));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0F));
    }

    public void setSuffocating(boolean p_33952_)
    {
        this.entityData.set(DATA_SUFFOCATING, p_33952_);
    }

    public boolean isSuffocating()
    {
        return this.getVehicle() instanceof Strider ? ((Strider)this.getVehicle()).isSuffocating() : this.entityData.get(DATA_SUFFOCATING);
    }

    public boolean canStandOnFluid(FluidState p_204067_)
    {
        return p_204067_.is(FluidTags.LAVA);
    }

    public double getPassengersRidingOffset()
    {
        float f = Math.min(0.25F, this.animationSpeed);
        float f1 = this.animationPosition;
        return (double)this.getBbHeight() - 0.19D + (double)(0.12F * Mth.cos(f1 * 1.5F) * 2.0F * f);
    }

    public boolean canBeControlledByRider()
    {
        Entity entity = this.getControllingPassenger();

        if (!(entity instanceof Player))
        {
            return false;
        }
        else
        {
            Player player = (Player)entity;
            return player.getMainHandItem().is(Items.WARPED_FUNGUS_ON_A_STICK) || player.getOffhandItem().is(Items.WARPED_FUNGUS_ON_A_STICK);
        }
    }

    public boolean checkSpawnObstruction(LevelReader pLevel)
    {
        return pLevel.isUnobstructed(this);
    }

    @Nullable
    public Entity getControllingPassenger()
    {
        return this.getFirstPassenger();
    }

    public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity)
    {
        Vec3[] avec3 = new Vec3[] {getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), pLivingEntity.getYRot()), getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), pLivingEntity.getYRot() - 22.5F), getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), pLivingEntity.getYRot() + 22.5F), getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), pLivingEntity.getYRot() - 45.0F), getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), pLivingEntity.getYRot() + 45.0F)};
        Set<BlockPos> set = Sets.newLinkedHashSet();
        double d0 = this.getBoundingBox().maxY;
        double d1 = this.getBoundingBox().minY - 0.5D;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (Vec3 vec3 : avec3)
        {
            blockpos$mutableblockpos.set(this.getX() + vec3.x, d0, this.getZ() + vec3.z);

            for (double d2 = d0; d2 > d1; --d2)
            {
                set.add(blockpos$mutableblockpos.immutable());
                blockpos$mutableblockpos.move(Direction.DOWN);
            }
        }

        for (BlockPos blockpos : set)
        {
            if (!this.level.getFluidState(blockpos).is(FluidTags.LAVA))
            {
                double d3 = this.level.getBlockFloorHeight(blockpos);

                if (DismountHelper.isBlockFloorValid(d3))
                {
                    Vec3 vec31 = Vec3.upFromBottomCenterOf(blockpos, d3);

                    for (Pose pose : pLivingEntity.getDismountPoses())
                    {
                        AABB aabb = pLivingEntity.getLocalBoundsForPose(pose);

                        if (DismountHelper.canDismountTo(this.level, pLivingEntity, aabb.move(vec31)))
                        {
                            pLivingEntity.setPose(pose);
                            return vec31;
                        }
                    }
                }
            }
        }

        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    public void travel(Vec3 pTravelVector)
    {
        this.setSpeed(this.getMoveSpeed());
        this.travel(this, this.steering, pTravelVector);
    }

    public float getMoveSpeed()
    {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.66F : 1.0F);
    }

    public float getSteeringSpeed()
    {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.23F : 0.55F);
    }

    public void travelWithInput(Vec3 pTravelVec)
    {
        super.travel(pTravelVec);
    }

    protected float nextStep()
    {
        return this.moveDist + 0.6F;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock)
    {
        this.playSound(this.isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0F, 1.0F);
    }

    public boolean boost()
    {
        return this.steering.boost(this.getRandom());
    }

    protected void checkFallDamage(double pY, boolean p_33871_, BlockState pOnGround, BlockPos pState)
    {
        this.checkInsideBlocks();

        if (this.isInLava())
        {
            this.resetFallDistance();
        }
        else
        {
            super.checkFallDamage(pY, p_33871_, pOnGround, pState);
        }
    }

    public void tick()
    {
        if (this.isBeingTempted() && this.random.nextInt(140) == 0)
        {
            this.playSound(SoundEvents.STRIDER_HAPPY, 1.0F, this.getVoicePitch());
        }
        else if (this.isPanicking() && this.random.nextInt(60) == 0)
        {
            this.playSound(SoundEvents.STRIDER_RETREAT, 1.0F, this.getVoicePitch());
        }

        BlockState blockstate = this.level.getBlockState(this.blockPosition());
        BlockState blockstate1 = this.getBlockStateOn();
        boolean flag = blockstate.is(BlockTags.STRIDER_WARM_BLOCKS) || blockstate1.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0D;
        this.setSuffocating(!flag);
        super.tick();
        this.floatStrider();
        this.checkInsideBlocks();
    }

    private boolean isPanicking()
    {
        return this.panicGoal != null && this.panicGoal.isRunning();
    }

    private boolean isBeingTempted()
    {
        return this.temptGoal != null && this.temptGoal.isRunning();
    }

    protected boolean shouldPassengersInheritMalus()
    {
        return true;
    }

    private void floatStrider()
    {
        if (this.isInLava())
        {
            CollisionContext collisioncontext = CollisionContext.of(this);

            if (collisioncontext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true) && !this.level.getFluidState(this.blockPosition().above()).is(FluidTags.LAVA))
            {
                this.onGround = true;
            }
            else
            {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D).add(0.0D, 0.05D, 0.0D));
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.175F).add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    protected SoundEvent getAmbientSound()
    {
        return !this.isPanicking() && !this.isBeingTempted() ? SoundEvents.STRIDER_AMBIENT : null;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource)
    {
        return SoundEvents.STRIDER_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.STRIDER_DEATH;
    }

    protected boolean canAddPassenger(Entity pPassenger)
    {
        return !this.isVehicle() && !this.isEyeInFluid(FluidTags.LAVA);
    }

    public boolean isSensitiveToWater()
    {
        return true;
    }

    public boolean isOnFire()
    {
        return false;
    }

    protected PathNavigation createNavigation(Level pLevel)
    {
        return new Strider.StriderPathNavigation(this, pLevel);
    }

    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel)
    {
        if (pLevel.getBlockState(pPos).getFluidState().is(FluidTags.LAVA))
        {
            return 10.0F;
        }
        else
        {
            return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
        }
    }

    public Strider getBreedOffspring(ServerLevel p_149861_, AgeableMob p_149862_)
    {
        return EntityType.STRIDER.create(p_149861_);
    }

    public boolean isFood(ItemStack pStack)
    {
        return FOOD_ITEMS.test(pStack);
    }

    protected void dropEquipment()
    {
        super.dropEquipment();

        if (this.isSaddled())
        {
            this.spawnAtLocation(Items.SADDLE);
        }
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand)
    {
        boolean flag = this.isFood(pPlayer.getItemInHand(pHand));

        if (!flag && this.isSaddled() && !this.isVehicle() && !pPlayer.isSecondaryUseActive())
        {
            if (!this.level.isClientSide)
            {
                pPlayer.startRiding(this);
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else
        {
            InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);

            if (!interactionresult.consumesAction())
            {
                ItemStack itemstack = pPlayer.getItemInHand(pHand);
                return itemstack.is(Items.SADDLE) ? itemstack.interactLivingEntity(pPlayer, this, pHand) : InteractionResult.PASS;
            }
            else
            {
                if (flag && !this.isSilent())
                {
                    this.level.playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.STRIDER_EAT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                }

                return interactionresult;
            }
        }
    }

    public Vec3 getLeashOffset()
    {
        return new Vec3(0.0D, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag)
    {
        if (this.isBaby())
        {
            return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        }
        else
        {
            Object object;

            if (this.random.nextInt(30) == 0)
            {
                Mob mob = EntityType.ZOMBIFIED_PIGLIN.create(pLevel.getLevel());
                object = this.spawnJockey(pLevel, pDifficulty, mob, new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(this.random), false));
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                this.equipSaddle((SoundSource)null);
            }
            else if (this.random.nextInt(10) == 0)
            {
                AgeableMob ageablemob = EntityType.STRIDER.create(pLevel.getLevel());
                ageablemob.setAge(-24000);
                object = this.spawnJockey(pLevel, pDifficulty, ageablemob, (SpawnGroupData)null);
            }
            else
            {
                object = new AgeableMob.AgeableMobGroupData(0.5F);
            }

            return super.finalizeSpawn(pLevel, pDifficulty, pReason, (SpawnGroupData)object, pDataTag);
        }
    }

    private SpawnGroupData spawnJockey(ServerLevelAccessor p_33882_, DifficultyInstance p_33883_, Mob p_33884_, @Nullable SpawnGroupData p_33885_)
    {
        p_33884_.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        p_33884_.finalizeSpawn(p_33882_, p_33883_, MobSpawnType.JOCKEY, p_33885_, (CompoundTag)null);
        p_33884_.startRiding(this, true);
        return new AgeableMob.AgeableMobGroupData(0.0F);
    }

    static class StriderGoToLavaGoal extends MoveToBlockGoal
    {
        private final Strider strider;

        StriderGoToLavaGoal(Strider p_33955_, double p_33956_)
        {
            super(p_33955_, p_33956_, 8, 2);
            this.strider = p_33955_;
        }

        public BlockPos getMoveToTarget()
        {
            return this.blockPos;
        }

        public boolean canContinueToUse()
        {
            return !this.strider.isInLava() && this.isValidTarget(this.strider.level, this.blockPos);
        }

        public boolean canUse()
        {
            return !this.strider.isInLava() && super.canUse();
        }

        public boolean shouldRecalculatePath()
        {
            return this.tryTicks % 20 == 0;
        }

        protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos)
        {
            return pLevel.getBlockState(pPos).is(Blocks.LAVA) && pLevel.getBlockState(pPos.above()).isPathfindable(pLevel, pPos, PathComputationType.LAND);
        }
    }

    static class StriderPathNavigation extends GroundPathNavigation
    {
        StriderPathNavigation(Strider p_33969_, Level p_33970_)
        {
            super(p_33969_, p_33970_);
        }

        protected PathFinder createPathFinder(int p_33972_)
        {
            this.nodeEvaluator = new WalkNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, p_33972_);
        }

        protected boolean hasValidPathType(BlockPathTypes p_33974_)
        {
            return p_33974_ != BlockPathTypes.LAVA && p_33974_ != BlockPathTypes.DAMAGE_FIRE && p_33974_ != BlockPathTypes.DANGER_FIRE ? super.hasValidPathType(p_33974_) : true;
        }

        public boolean isStableDestination(BlockPos pPos)
        {
            return this.level.getBlockState(pPos).is(Blocks.LAVA) || super.isStableDestination(pPos);
        }
    }
}
