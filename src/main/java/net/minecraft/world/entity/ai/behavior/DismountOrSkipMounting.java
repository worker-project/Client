package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class DismountOrSkipMounting<E extends LivingEntity, T extends Entity> extends Behavior<E>
{
    private final int maxWalkDistToRideTarget;
    private final BiPredicate<E, Entity> dontRideIf;

    public DismountOrSkipMounting(int p_22827_, BiPredicate<E, Entity> p_22828_)
    {
        super(ImmutableMap.of(MemoryModuleType.RIDE_TARGET, MemoryStatus.REGISTERED));
        this.maxWalkDistToRideTarget = p_22827_;
        this.dontRideIf = p_22828_;
    }

    protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner)
    {
        Entity entity = pOwner.getVehicle();
        Entity entity1 = pOwner.getBrain().getMemory(MemoryModuleType.RIDE_TARGET).orElse((Entity)null);

        if (entity == null && entity1 == null)
        {
            return false;
        }
        else
        {
            Entity entity2 = entity == null ? entity1 : entity;
            return !this.isVehicleValid(pOwner, entity2) || this.dontRideIf.test(pOwner, entity2);
        }
    }

    private boolean isVehicleValid(E p_22837_, Entity p_22838_)
    {
        return p_22838_.isAlive() && p_22838_.closerThan(p_22837_, (double)this.maxWalkDistToRideTarget) && p_22838_.level == p_22837_.level;
    }

    protected void start(ServerLevel pLevel, E pEntity, long pGameTime)
    {
        pEntity.stopRiding();
        pEntity.getBrain().eraseMemory(MemoryModuleType.RIDE_TARGET);
    }
}
