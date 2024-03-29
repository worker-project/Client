package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTables extends SimpleJsonResourceReloadListener
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private Map<ResourceLocation, LootTable> tables = ImmutableMap.of();
    private final PredicateManager predicateManager;

    public LootTables(PredicateManager pPredicateManager)
    {
        super(GSON, "loot_tables");
        this.predicateManager = pPredicateManager;
    }

    public LootTable get(ResourceLocation pLootTableId)
    {
        return this.tables.getOrDefault(pLootTableId, LootTable.EMPTY);
    }

    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        Builder<ResourceLocation, LootTable> builder = ImmutableMap.builder();
        JsonElement jsonelement = pObject.remove(BuiltInLootTables.EMPTY);

        if (jsonelement != null)
        {
            LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", (Object)BuiltInLootTables.EMPTY);
        }

        pObject.forEach((p_79198_, p_79199_) ->
        {
            try {
                LootTable loottable = GSON.fromJson(p_79199_, LootTable.class);
                builder.put(p_79198_, loottable);
            }
            catch (Exception exception)
            {
                LOGGER.error("Couldn't parse loot table {}", p_79198_, exception);
            }
        });
        builder.put(BuiltInLootTables.EMPTY, LootTable.EMPTY);
        ImmutableMap<ResourceLocation, LootTable> immutablemap = builder.build();
        ValidationContext validationcontext = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, immutablemap::get);
        immutablemap.forEach((p_79221_, p_79222_) ->
        {
            validate(validationcontext, p_79221_, p_79222_);
        });
        validationcontext.getProblems().forEach((p_79211_, p_79212_) ->
        {
            LOGGER.warn("Found validation problem in {}: {}", p_79211_, p_79212_);
        });
        this.tables = immutablemap;
    }

    public static void validate(ValidationContext pValidator, ResourceLocation pId, LootTable pLootTable)
    {
        pLootTable.validate(pValidator.setParams(pLootTable.getParamSet()).enterTable("{" + pId + "}", pId));
    }

    public static JsonElement serialize(LootTable pLootTable)
    {
        return GSON.toJsonTree(pLootTable);
    }

    public Set<ResourceLocation> getIds()
    {
        return this.tables.keySet();
    }
}
