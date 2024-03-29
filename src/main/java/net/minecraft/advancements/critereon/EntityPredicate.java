package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityPredicate
{
    public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, LocationPredicate.ANY, MobEffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY, PlayerPredicate.ANY, FishingHookPredicate.ANY, LighthingBoltPredicate.ANY, (String)null, (ResourceLocation)null);
    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final LocationPredicate steppingOnLocation;
    private final MobEffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final PlayerPredicate player;
    private final FishingHookPredicate fishingHook;
    private final LighthingBoltPredicate lighthingBolt;
    private final EntityPredicate vehicle;
    private final EntityPredicate passenger;
    private final EntityPredicate targetedEntity;
    @Nullable
    private final String team;
    @Nullable
    private final ResourceLocation catType;

    private EntityPredicate(EntityTypePredicate pEntityType, DistancePredicate pDistanceToPlayer, LocationPredicate pLocation, LocationPredicate pSteppingOnLocation, MobEffectsPredicate pEffects, NbtPredicate pNbt, EntityFlagsPredicate pFlags, EntityEquipmentPredicate pEquipment, PlayerPredicate pPlayer, FishingHookPredicate pFishingHook, LighthingBoltPredicate pLightningBolt, @Nullable String pTeam, @Nullable ResourceLocation pCatType)
    {
        this.entityType = pEntityType;
        this.distanceToPlayer = pDistanceToPlayer;
        this.location = pLocation;
        this.steppingOnLocation = pSteppingOnLocation;
        this.effects = pEffects;
        this.nbt = pNbt;
        this.flags = pFlags;
        this.equipment = pEquipment;
        this.player = pPlayer;
        this.fishingHook = pFishingHook;
        this.lighthingBolt = pLightningBolt;
        this.passenger = this;
        this.vehicle = this;
        this.targetedEntity = this;
        this.team = pTeam;
        this.catType = pCatType;
    }

    EntityPredicate(EntityTypePredicate pEntityType, DistancePredicate pDistanceToPlayer, LocationPredicate pLocation, LocationPredicate pSteppingOnLocation, MobEffectsPredicate pEffects, NbtPredicate pNbt, EntityFlagsPredicate pFlags, EntityEquipmentPredicate pEquipment, PlayerPredicate pPlayer, FishingHookPredicate pFishingHook, LighthingBoltPredicate pLightningBolt, EntityPredicate pVehicle, EntityPredicate pPassenger, EntityPredicate pTargetedEntity, @Nullable String pTeam, @Nullable ResourceLocation pCatType)
    {
        this.entityType = pEntityType;
        this.distanceToPlayer = pDistanceToPlayer;
        this.location = pLocation;
        this.steppingOnLocation = pSteppingOnLocation;
        this.effects = pEffects;
        this.nbt = pNbt;
        this.flags = pFlags;
        this.equipment = pEquipment;
        this.player = pPlayer;
        this.fishingHook = pFishingHook;
        this.lighthingBolt = pLightningBolt;
        this.vehicle = pVehicle;
        this.passenger = pPassenger;
        this.targetedEntity = pTargetedEntity;
        this.team = pTeam;
        this.catType = pCatType;
    }

    public boolean matches(ServerPlayer pPlayer, @Nullable Entity pEntity)
    {
        return this.matches(pPlayer.getLevel(), pPlayer.position(), pEntity);
    }

    public boolean matches(ServerLevel pLevel, @Nullable Vec3 pPosition, @Nullable Entity pEntity)
    {
        if (this == ANY)
        {
            return true;
        }
        else if (pEntity == null)
        {
            return false;
        }
        else if (!this.entityType.matches(pEntity.getType()))
        {
            return false;
        }
        else
        {
            if (pPosition == null)
            {
                if (this.distanceToPlayer != DistancePredicate.ANY)
                {
                    return false;
                }
            }
            else if (!this.distanceToPlayer.matches(pPosition.x, pPosition.y, pPosition.z, pEntity.getX(), pEntity.getY(), pEntity.getZ()))
            {
                return false;
            }

            if (!this.location.matches(pLevel, pEntity.getX(), pEntity.getY(), pEntity.getZ()))
            {
                return false;
            }
            else
            {
                if (this.steppingOnLocation != LocationPredicate.ANY)
                {
                    Vec3 vec3 = Vec3.atCenterOf(pEntity.getOnPos());

                    if (!this.steppingOnLocation.matches(pLevel, vec3.x(), vec3.y(), vec3.z()))
                    {
                        return false;
                    }
                }

                if (!this.effects.matches(pEntity))
                {
                    return false;
                }
                else if (!this.nbt.matches(pEntity))
                {
                    return false;
                }
                else if (!this.flags.matches(pEntity))
                {
                    return false;
                }
                else if (!this.equipment.matches(pEntity))
                {
                    return false;
                }
                else if (!this.player.matches(pEntity))
                {
                    return false;
                }
                else if (!this.fishingHook.matches(pEntity))
                {
                    return false;
                }
                else if (!this.lighthingBolt.matches(pEntity, pLevel, pPosition))
                {
                    return false;
                }
                else if (!this.vehicle.matches(pLevel, pPosition, pEntity.getVehicle()))
                {
                    return false;
                }
                else if (this.passenger != ANY && pEntity.getPassengers().stream().noneMatch((p_150322_) ->
            {
                return this.passenger.matches(pLevel, pPosition, p_150322_);
                }))
                {
                    return false;
                }
                else if (!this.targetedEntity.matches(pLevel, pPosition, pEntity instanceof Mob ? ((Mob)pEntity).getTarget() : null))
                {
                    return false;
                }
                else
                {
                    if (this.team != null)
                    {
                        Team team = pEntity.getTeam();

                        if (team == null || !this.team.equals(team.getName()))
                        {
                            return false;
                        }
                    }

                    return this.catType == null || pEntity instanceof Cat && ((Cat)pEntity).getResourceLocation().equals(this.catType);
                }
            }
        }
    }

    public static EntityPredicate fromJson(@Nullable JsonElement pJson)
    {
        if (pJson != null && !pJson.isJsonNull())
        {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "entity");
            EntityTypePredicate entitytypepredicate = EntityTypePredicate.fromJson(jsonobject.get("type"));
            DistancePredicate distancepredicate = DistancePredicate.fromJson(jsonobject.get("distance"));
            LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject.get("location"));
            LocationPredicate locationpredicate1 = LocationPredicate.fromJson(jsonobject.get("stepping_on"));
            MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(jsonobject.get("effects"));
            NbtPredicate nbtpredicate = NbtPredicate.fromJson(jsonobject.get("nbt"));
            EntityFlagsPredicate entityflagspredicate = EntityFlagsPredicate.fromJson(jsonobject.get("flags"));
            EntityEquipmentPredicate entityequipmentpredicate = EntityEquipmentPredicate.fromJson(jsonobject.get("equipment"));
            PlayerPredicate playerpredicate = PlayerPredicate.fromJson(jsonobject.get("player"));
            FishingHookPredicate fishinghookpredicate = FishingHookPredicate.fromJson(jsonobject.get("fishing_hook"));
            EntityPredicate entitypredicate = fromJson(jsonobject.get("vehicle"));
            EntityPredicate entitypredicate1 = fromJson(jsonobject.get("passenger"));
            EntityPredicate entitypredicate2 = fromJson(jsonobject.get("targeted_entity"));
            LighthingBoltPredicate lighthingboltpredicate = LighthingBoltPredicate.fromJson(jsonobject.get("lightning_bolt"));
            String s = GsonHelper.getAsString(jsonobject, "team", (String)null);
            ResourceLocation resourcelocation = jsonobject.has("catType") ? new ResourceLocation(GsonHelper.getAsString(jsonobject, "catType")) : null;
            return (new EntityPredicate.Builder()).entityType(entitytypepredicate).distance(distancepredicate).located(locationpredicate).steppingOn(locationpredicate1).effects(mobeffectspredicate).nbt(nbtpredicate).flags(entityflagspredicate).equipment(entityequipmentpredicate).player(playerpredicate).fishingHook(fishinghookpredicate).lighthingBolt(lighthingboltpredicate).team(s).vehicle(entitypredicate).passenger(entitypredicate1).targetedEntity(entitypredicate2).catType(resourcelocation).build();
        }
        else
        {
            return ANY;
        }
    }

    public JsonElement serializeToJson()
    {
        if (this == ANY)
        {
            return JsonNull.INSTANCE;
        }
        else
        {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("type", this.entityType.serializeToJson());
            jsonobject.add("distance", this.distanceToPlayer.serializeToJson());
            jsonobject.add("location", this.location.serializeToJson());
            jsonobject.add("stepping_on", this.steppingOnLocation.serializeToJson());
            jsonobject.add("effects", this.effects.serializeToJson());
            jsonobject.add("nbt", this.nbt.serializeToJson());
            jsonobject.add("flags", this.flags.serializeToJson());
            jsonobject.add("equipment", this.equipment.serializeToJson());
            jsonobject.add("player", this.player.serializeToJson());
            jsonobject.add("fishing_hook", this.fishingHook.serializeToJson());
            jsonobject.add("lightning_bolt", this.lighthingBolt.serializeToJson());
            jsonobject.add("vehicle", this.vehicle.serializeToJson());
            jsonobject.add("passenger", this.passenger.serializeToJson());
            jsonobject.add("targeted_entity", this.targetedEntity.serializeToJson());
            jsonobject.addProperty("team", this.team);

            if (this.catType != null)
            {
                jsonobject.addProperty("catType", this.catType.toString());
            }

            return jsonobject;
        }
    }

    public static LootContext createContext(ServerPlayer pPlayer, Entity pEntity)
    {
        return (new LootContext.Builder(pPlayer.getLevel())).withParameter(LootContextParams.THIS_ENTITY, pEntity).withParameter(LootContextParams.ORIGIN, pPlayer.position()).withRandom(pPlayer.getRandom()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
    }

    public static class Builder
    {
        private EntityTypePredicate entityType = EntityTypePredicate.ANY;
        private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;
        private LocationPredicate steppingOnLocation = LocationPredicate.ANY;
        private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;
        private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
        private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
        private PlayerPredicate player = PlayerPredicate.ANY;
        private FishingHookPredicate fishingHook = FishingHookPredicate.ANY;
        private LighthingBoltPredicate lighthingBolt = LighthingBoltPredicate.ANY;
        private EntityPredicate vehicle = EntityPredicate.ANY;
        private EntityPredicate passenger = EntityPredicate.ANY;
        private EntityPredicate targetedEntity = EntityPredicate.ANY;
        @Nullable
        private String team;
        @Nullable
        private ResourceLocation catType;

        public static EntityPredicate.Builder entity()
        {
            return new EntityPredicate.Builder();
        }

        public EntityPredicate.Builder of(EntityType<?> pCatType)
        {
            this.entityType = EntityTypePredicate.of(pCatType);
            return this;
        }

        public EntityPredicate.Builder of(TagKey < EntityType<? >> pCatType)
        {
            this.entityType = EntityTypePredicate.of(pCatType);
            return this;
        }

        public EntityPredicate.Builder of(ResourceLocation pCatType)
        {
            this.catType = pCatType;
            return this;
        }

        public EntityPredicate.Builder entityType(EntityTypePredicate pEntityType)
        {
            this.entityType = pEntityType;
            return this;
        }

        public EntityPredicate.Builder distance(DistancePredicate pDistanceToPlayer)
        {
            this.distanceToPlayer = pDistanceToPlayer;
            return this;
        }

        public EntityPredicate.Builder located(LocationPredicate pLocation)
        {
            this.location = pLocation;
            return this;
        }

        public EntityPredicate.Builder steppingOn(LocationPredicate pSteppingOnLocation)
        {
            this.steppingOnLocation = pSteppingOnLocation;
            return this;
        }

        public EntityPredicate.Builder effects(MobEffectsPredicate pEffects)
        {
            this.effects = pEffects;
            return this;
        }

        public EntityPredicate.Builder nbt(NbtPredicate pNbt)
        {
            this.nbt = pNbt;
            return this;
        }

        public EntityPredicate.Builder flags(EntityFlagsPredicate pFlags)
        {
            this.flags = pFlags;
            return this;
        }

        public EntityPredicate.Builder equipment(EntityEquipmentPredicate pEquipment)
        {
            this.equipment = pEquipment;
            return this;
        }

        public EntityPredicate.Builder player(PlayerPredicate pPlayer)
        {
            this.player = pPlayer;
            return this;
        }

        public EntityPredicate.Builder fishingHook(FishingHookPredicate pFishing)
        {
            this.fishingHook = pFishing;
            return this;
        }

        public EntityPredicate.Builder lighthingBolt(LighthingBoltPredicate pLightningBolt)
        {
            this.lighthingBolt = pLightningBolt;
            return this;
        }

        public EntityPredicate.Builder vehicle(EntityPredicate pVehicle)
        {
            this.vehicle = pVehicle;
            return this;
        }

        public EntityPredicate.Builder passenger(EntityPredicate pPassenger)
        {
            this.passenger = pPassenger;
            return this;
        }

        public EntityPredicate.Builder targetedEntity(EntityPredicate pTargetedEntity)
        {
            this.targetedEntity = pTargetedEntity;
            return this;
        }

        public EntityPredicate.Builder team(@Nullable String pTeam)
        {
            this.team = pTeam;
            return this;
        }

        public EntityPredicate.Builder catType(@Nullable ResourceLocation pCatType)
        {
            this.catType = pCatType;
            return this;
        }

        public EntityPredicate build()
        {
            return new EntityPredicate(this.entityType, this.distanceToPlayer, this.location, this.steppingOnLocation, this.effects, this.nbt, this.flags, this.equipment, this.player, this.fishingHook, this.lighthingBolt, this.vehicle, this.passenger, this.targetedEntity, this.team, this.catType);
        }
    }

    public static class Composite
    {
        public static final EntityPredicate.Composite ANY = new EntityPredicate.Composite(new LootItemCondition[0]);
        private final LootItemCondition[] conditions;
        private final Predicate<LootContext> compositePredicates;

        private Composite(LootItemCondition[] pConditions)
        {
            this.conditions = pConditions;
            this.compositePredicates = LootItemConditions.a(pConditions);
        }

        public static EntityPredicate.Composite a(LootItemCondition... p_36691_)
        {
            return new EntityPredicate.Composite(p_36691_);
        }

        public static EntityPredicate.Composite fromJson(JsonObject pJson, String pProperty, DeserializationContext pContext)
        {
            JsonElement jsonelement = pJson.get(pProperty);
            return fromElement(pProperty, pContext, jsonelement);
        }

        public static EntityPredicate.Composite[] fromJsonArray(JsonObject pJson, String pProperty, DeserializationContext pContext)
        {
            JsonElement jsonelement = pJson.get(pProperty);

            if (jsonelement != null && !jsonelement.isJsonNull())
            {
                JsonArray jsonarray = GsonHelper.convertToJsonArray(jsonelement, pProperty);
                EntityPredicate.Composite[] aentitypredicate$composite = new EntityPredicate.Composite[jsonarray.size()];

                for (int i = 0; i < jsonarray.size(); ++i)
                {
                    aentitypredicate$composite[i] = fromElement(pProperty + "[" + i + "]", pContext, jsonarray.get(i));
                }

                return aentitypredicate$composite;
            }
            else
            {
                return new EntityPredicate.Composite[0];
            }
        }

        private static EntityPredicate.Composite fromElement(String pName, DeserializationContext pContext, @Nullable JsonElement pJson)
        {
            if (pJson != null && pJson.isJsonArray())
            {
                LootItemCondition[] alootitemcondition = pContext.deserializeConditions(pJson.getAsJsonArray(), pContext.getAdvancementId() + "/" + pName, LootContextParamSets.ADVANCEMENT_ENTITY);
                return new EntityPredicate.Composite(alootitemcondition);
            }
            else
            {
                EntityPredicate entitypredicate = EntityPredicate.fromJson(pJson);
                return wrap(entitypredicate);
            }
        }

        public static EntityPredicate.Composite wrap(EntityPredicate pEntityCondition)
        {
            if (pEntityCondition == EntityPredicate.ANY)
            {
                return ANY;
            }
            else
            {
                LootItemCondition lootitemcondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, pEntityCondition).build();
                return new EntityPredicate.Composite(new LootItemCondition[] {lootitemcondition});
            }
        }

        public boolean matches(LootContext pLootContext)
        {
            return this.compositePredicates.test(pLootContext);
        }

        public JsonElement toJson(SerializationContext pContext)
        {
            return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : pContext.a(this.conditions));
        }

        public static JsonElement a(EntityPredicate.Composite[] p_36688_, SerializationContext p_36689_)
        {
            if (p_36688_.length == 0)
            {
                return JsonNull.INSTANCE;
            }
            else
            {
                JsonArray jsonarray = new JsonArray();

                for (EntityPredicate.Composite entitypredicate$composite : p_36688_)
                {
                    jsonarray.add(entitypredicate$composite.toJson(p_36689_));
                }

                return jsonarray;
            }
        }
    }
}
