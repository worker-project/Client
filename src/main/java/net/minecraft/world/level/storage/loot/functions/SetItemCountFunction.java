package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetItemCountFunction extends LootItemConditionalFunction
{
    final NumberProvider value;
    final boolean add;

    SetItemCountFunction(LootItemCondition[] pConditions, NumberProvider pCountValue, boolean pAdd)
    {
        super(pConditions);
        this.value = pCountValue;
        this.add = pAdd;
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.SET_COUNT;
    }

    public Set < LootContextParam<? >> getReferencedContextParams()
    {
        return this.value.getReferencedContextParams();
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        int i = this.add ? pStack.getCount() : 0;
        pStack.setCount(Mth.clamp(i + this.value.getInt(pContext), 0, pStack.getMaxStackSize()));
        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider pCountValue)
    {
        return simpleBuilder((p_165423_) ->
        {
            return new SetItemCountFunction(p_165423_, pCountValue, false);
        });
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider pCountValue, boolean pAdd)
    {
        return simpleBuilder((p_165420_) ->
        {
            return new SetItemCountFunction(p_165420_, pCountValue, pAdd);
        });
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemCountFunction>
    {
        public void serialize(JsonObject pJson, SetItemCountFunction pValue, JsonSerializationContext pSerializationContext)
        {
            super.serialize(pJson, pValue, pSerializationContext);
            pJson.add("count", pSerializationContext.serialize(pValue.value));
            pJson.addProperty("add", pValue.add);
        }

        public SetItemCountFunction b(JsonObject p_81018_, JsonDeserializationContext p_81019_, LootItemCondition[] p_81020_)
        {
            NumberProvider numberprovider = GsonHelper.getAsObject(p_81018_, "count", p_81019_, NumberProvider.class);
            boolean flag = GsonHelper.getAsBoolean(p_81018_, "add", false);
            return new SetItemCountFunction(p_81020_, numberprovider, flag);
        }
    }
}
