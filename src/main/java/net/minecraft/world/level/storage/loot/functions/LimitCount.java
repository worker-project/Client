package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction
{
    final IntRange limiter;

    LimitCount(LootItemCondition[] pConditions, IntRange pCountLimit)
    {
        super(pConditions);
        this.limiter = pCountLimit;
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.LIMIT_COUNT;
    }

    public Set < LootContextParam<? >> getReferencedContextParams()
    {
        return this.limiter.getReferencedContextParams();
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        int i = this.limiter.clamp(pContext, pStack.getCount());
        pStack.setCount(i);
        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> limitCount(IntRange pCountLimit)
    {
        return simpleBuilder((p_165219_) ->
        {
            return new LimitCount(p_165219_, pCountLimit);
        });
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LimitCount>
    {
        public void serialize(JsonObject pJson, LimitCount pValue, JsonSerializationContext pSerializationContext)
        {
            super.serialize(pJson, pValue, pSerializationContext);
            pJson.add("limit", pSerializationContext.serialize(pValue.limiter));
        }

        public LimitCount b(JsonObject p_80656_, JsonDeserializationContext p_80657_, LootItemCondition[] p_80658_)
        {
            IntRange intrange = GsonHelper.getAsObject(p_80656_, "limit", p_80657_, IntRange.class);
            return new LimitCount(p_80658_, intrange);
        }
    }
}
