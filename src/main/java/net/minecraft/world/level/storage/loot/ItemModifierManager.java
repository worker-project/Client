package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class ItemModifierManager extends SimpleJsonResourceReloadListener
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = Deserializers.createFunctionSerializer().create();
    private final PredicateManager predicateManager;
    private final LootTables lootTables;
    private Map<ResourceLocation, LootItemFunction> functions = ImmutableMap.of();

    public ItemModifierManager(PredicateManager pPredicateManager, LootTables pLootTables)
    {
        super(GSON, "item_modifiers");
        this.predicateManager = pPredicateManager;
        this.lootTables = pLootTables;
    }

    @Nullable
    public LootItemFunction get(ResourceLocation pFunctionId)
    {
        return this.functions.get(pFunctionId);
    }

    public LootItemFunction get(ResourceLocation pFunctionId, LootItemFunction pDefaultFunction)
    {
        return this.functions.getOrDefault(pFunctionId, pDefaultFunction);
    }

    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        Builder<ResourceLocation, LootItemFunction> builder = ImmutableMap.builder();
        pObject.forEach((p_165091_, p_165092_) ->
        {
            try {
                if (p_165092_.isJsonArray())
                {
                    LootItemFunction[] alootitemfunction = GSON.fromJson(p_165092_, LootItemFunction[].class);
                    builder.put(p_165091_, new ItemModifierManager.FunctionSequence(alootitemfunction));
                }
                else {
                    LootItemFunction lootitemfunction = GSON.fromJson(p_165092_, LootItemFunction.class);
                    builder.put(p_165091_, lootitemfunction);
                }
            }
            catch (Exception exception)
            {
                LOGGER.error("Couldn't parse item modifier {}", p_165091_, exception);
            }
        });
        Map<ResourceLocation, LootItemFunction> map = builder.build();
        ValidationContext validationcontext = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, this.lootTables::get);
        map.forEach((p_165095_, p_165096_) ->
        {
            p_165096_.validate(validationcontext);
        });
        validationcontext.getProblems().forEach((p_165102_, p_165103_) ->
        {
            LOGGER.warn("Found item modifier validation problem in {}: {}", p_165102_, p_165103_);
        });
        this.functions = map;
    }

    public Set<ResourceLocation> getKeys()
    {
        return Collections.unmodifiableSet(this.functions.keySet());
    }

    static class FunctionSequence implements LootItemFunction
    {
        protected final LootItemFunction[] functions;
        private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

        public FunctionSequence(LootItemFunction[] p_165116_)
        {
            this.functions = p_165116_;
            this.compositeFunction = LootItemFunctions.a(p_165116_);
        }

        public ItemStack apply(ItemStack p_165119_, LootContext p_165120_)
        {
            return this.compositeFunction.apply(p_165119_, p_165120_);
        }

        public LootItemFunctionType getType()
        {
            throw new UnsupportedOperationException();
        }
    }
}
