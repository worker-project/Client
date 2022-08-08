package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot extends LootPoolSingletonContainer
{
    final ResourceLocation name;

    DynamicLoot(ResourceLocation pDynamicDropsName, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions)
    {
        super(pWeight, pQuality, pConditions, pFunctions);
        this.name = pDynamicDropsName;
    }

    public LootPoolEntryType getType()
    {
        return LootPoolEntries.DYNAMIC;
    }

    public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext)
    {
        pLootContext.addDynamicDrops(this.name, pStackConsumer);
    }

    public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation pDynamicDropsName)
    {
        return simpleBuilder((p_79487_, p_79488_, p_79489_, p_79490_) ->
        {
            return new DynamicLoot(pDynamicDropsName, p_79487_, p_79488_, p_79489_, p_79490_);
        });
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<DynamicLoot>
    {
        public void serializeCustom(JsonObject pObject, DynamicLoot pContext, JsonSerializationContext pConditions)
        {
            super.serializeCustom(pObject, pContext, pConditions);
            pObject.addProperty("name", pContext.name.toString());
        }

        protected DynamicLoot b(JsonObject p_79493_, JsonDeserializationContext p_79494_, int p_79495_, int p_79496_, LootItemCondition[] p_79497_, LootItemFunction[] p_79498_)
        {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_79493_, "name"));
            return new DynamicLoot(resourcelocation, p_79495_, p_79496_, p_79497_, p_79498_);
        }
    }
}
