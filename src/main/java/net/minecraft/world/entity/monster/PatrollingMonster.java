package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public abstract class PatrollingMonster extends Monster
{
    @Nullable
    private BlockPos patrolTarget;
    private boolean patrolLeader;
    private boolean patrolling;

    protected PatrollingMonster(EntityType <? extends PatrollingMonster > p_33046_, Level p_33047_)
    {
        super(p_33046_, p_33047_);
    }

    protected void registerGoals()
    {
        super.registerGoals();
        this.goalSelector.addGoal(4, new PatrollingMonster.LongDistancePatrolGoal<>(this, 0.7D, 0.595D));
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);

        if (this.patrolTarget != null)
        {
            pCompound.put("PatrolTarget", NbtUtils.writeBlockPos(this.patrolTarget));
        }

        pCompound.putBoolean("PatrolLeader", this.patrolLeader);
        pCompound.putBoolean("Patrolling", this.patrolling);
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.contains("PatrolTarget"))
        {
            this.patrolTarget = NbtUtils.readBlockPos(pCompound.getCompound("PatrolTarget"));
        }

        this.patrolLeader = pCompound.getBoolean("PatrolLeader");
        this.patrolling = pCompound.getBoolean("Patrolling");
    }

    public double getMyRidingOffset()
    {
        return -0.45D;
    }

    public boolean canBeLeader()
    {
        return true;
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag)
    {
        if (pReason != MobSpawnType.PATROL && pReason != MobSpawnType.EVENT && pReason != MobSpawnType.STRUCTURE && this.random.nextFloat() < 0.06F && this.canBeLeader())
        {
            this.patrolLeader = true;
        }

        if (this.isPatrolLeader())
        {
            this.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance());
            this.setDropChance(EquipmentSlot.HEAD, 2.0F);
        }

        if (pReason == MobSpawnType.PATROL)
        {
            this.patrolling = true;
        }

        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    public static boolean checkPatrollingMonsterSpawnRules(EntityType <? extends PatrollingMonster > p_33057_, LevelAccessor p_33058_, MobSpawnType p_33059_, BlockPos p_33060_, Random p_33061_)
    {
        return p_33058_.getBrightness(LightLayer.BLOCK, p_33060_) > 8 ? false : checkAnyLightMonsterSpawnRules(p_33057_, p_33058_, p_33059_, p_33060_, p_33061_);
    }

    public boolean removeWhenFarAway(double pDistanceToClosestPlayer)
    {
        return !this.patrolling || pDistanceToClosestPlayer > 16384.0D;
    }

    public void setPatrolTarget(BlockPos p_33071_)
    {
        this.patrolTarget = p_33071_;
        this.patrolling = true;
    }

    public BlockPos getPatrolTarget()
    {
        return this.patrolTarget;
    }

    public boolean hasPatrolTarget()
    {
        return this.patrolTarget != null;
    }

    public void setPatrolLeader(boolean pIsLeader)
    {
        this.patrolLeader = pIsLeader;
        this.patrolling = true;
    }

    public boolean isPatrolLeader()
    {
        return this.patrolLeader;
    }

    public boolean canJoinPatrol()
    {
        return true;
    }

    public void findPatrolTarget()
    {
        this.patrolTarget = this.blockPosition().offset(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
        this.patrolling = true;
    }

    protected boolean isPatrolling()
    {
        return this.patrolling;
    }

    protected void setPatrolling(boolean p_33078_)
    {
        this.patrolling = p_33078_;
    }

    public static class LongDistancePatrolGoal<T extends PatrollingMonster> extends Goal
    {
        private static final int NAVIGATION_FAILED_COOLDOWN = 200;
        private final T mob;
        private final double speedModifier;
        private final double leaderSpeedModifier;
        private long cooldownUntil;

        public LongDistancePatrolGoal(T p_33084_, double p_33085_, double p_33086_)
        {
            this.mob = p_33084_;
            this.speedModifier = p_33085_;
            this.leaderSpeedModifier = p_33086_;
            this.cooldownUntil = -1L;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse()
        {
            boolean flag = this.mob.level.getGameTime() < this.cooldownUntil;
            return this.mob.isPatrolling() && this.mob.getTarget() == null && !this.mob.isVehicle() && this.mob.hasPatrolTarget() && !flag;
        }

        public void start()
        {
        }

        public void stop()
        {
        }

        public void tick()
        {
            boolean flag = this.mob.isPatrolLeader();
            PathNavigation pathnavigation = this.mob.getNavigation();

            if (pathnavigation.isDone())
            {
                List<PatrollingMonster> list = this.findPatrolCompanions();

                if (this.mob.isPatrolling() && list.isEmpty())
                {
                    this.mob.setPatrolling(false);
                }
                else if (flag && this.mob.getPatrolTarget().closerToCenterThan(this.mob.position(), 10.0D))
                {
                    this.mob.findPatrolTarget();
                }
                else
                {
                    Vec3 vec3 = Vec3.atBottomCenterOf(this.mob.getPatrolTarget());
                    Vec3 vec31 = this.mob.position();
                    Vec3 vec32 = vec31.subtract(vec3);
                    vec3 = vec32.yRot(90.0F).scale(0.4D).add(vec3);
                    Vec3 vec33 = vec3.subtract(vec31).normalize().scale(10.0D).add(vec31);
                    BlockPos blockpos = new BlockPos(vec33);
                    blockpos = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos);

                    if (!pathnavigation.moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), flag ? this.leaderSpeedModifier : this.speedModifier))
                    {
                        this.moveRandomly();
                        this.cooldownUntil = this.mob.level.getGameTime() + 200L;
                    }
                    else if (flag)
                    {
                        for (PatrollingMonster patrollingmonster : list)
                        {
                            patrollingmonster.setPatrolTarget(blockpos);
                        }
                    }
                }
            }
        }

        private List<PatrollingMonster> findPatrolCompanions()
        {
            return this.mob.level.getEntitiesOfClass(PatrollingMonster.class, this.mob.getBoundingBox().inflate(16.0D), (p_33089_) ->
            {
                return p_33089_.canJoinPatrol() && !p_33089_.is(this.mob);
            });
        }

        private boolean moveRandomly()
        {
            Random random = this.mob.getRandom();
            BlockPos blockpos = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset(-8 + random.nextInt(16), 0, -8 + random.nextInt(16)));
            return this.mob.getNavigation().moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), this.speedModifier);
        }
    }
}