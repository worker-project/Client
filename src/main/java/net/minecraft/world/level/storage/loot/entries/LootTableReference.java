package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableReference extends LootPoolSingletonContainer
{
    final ResourceLocation name;

    LootTableReference(ResourceLocation pLootTableId, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions)
    {
        super(pWeight, pQuality, pConditions, pFunctions);
        this.name = pLootTableId;
    }

    public LootPoolEntryType getType()
    {
        return LootPoolEntries.REFERENCE;
    }

    public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext)
    {
        LootTable loottable = pLootContext.getLootTable(this.name);
        loottable.getRandomItemsRaw(pLootContext, pStackConsumer);
    }

    public void validate(ValidationContext pValidationContext)
    {
        if (pValidationContext.hasVisitedTable(this.name))
        {
            pValidationContext.reportProblem("Table " + this.name + " is recursively called");
        }
        else
        {
            super.validate(pValidationContext);
            LootTable loottable = pValidationContext.resolveLootTable(this.name);

            if (loottable == null)
            {
                pValidationContext.reportProblem("Unknown loot table called " + this.name);
            }
            else
            {
                loottable.validate(pValidationContext.enterTable("->{" + this.name + "}", this.name));
            }
        }
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation pTable)
    {
        return simpleBuilder((p_79780_, p_79781_, p_79782_, p_79783_) ->
        {
            return new LootTableReference(pTable, p_79780_, p_79781_, p_79782_, p_79783_);
        });
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<LootTableReference>
    {
        public void serializeCustom(JsonObject pObject, LootTableReference pContext, JsonSerializationContext pConditions)
        {
            super.serializeCustom(pObject, pContext, pConditions);
            pObject.addProperty("name", pContext.name.toString());
        }

        protected LootTableReference b(JsonObject p_79786_, JsonDeserializationContext p_79787_, int p_79788_, int p_79789_, LootItemCondition[] p_79790_, LootItemFunction[] p_79791_)
        {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_79786_, "name"));
            return new LootTableReference(resourcelocation, p_79788_, p_79789_, p_79790_, p_79791_);
        }
    }
}
