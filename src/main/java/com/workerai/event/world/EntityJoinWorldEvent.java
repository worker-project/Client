package com.workerai.event.world;

import com.workerai.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityJoinWorldEvent extends Event {
    private final LivingEntity entity;

    public EntityJoinWorldEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
