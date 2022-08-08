package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.slf4j.Logger;

public class SetItemDamageFunction extends LootItemConditionalFunction
{
    private static final Logger LOGGER = LogUtils.getLogger();
    final NumberProvider damage;
    final boolean add;

    SetItemDamageFunction(LootItemCondition[] pConditions, NumberProvider pDamageValue, boolean pAdd)
    {
        super(pConditions);
        this.damage = pDamageValue;
        this.add = pAdd;
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.SET_DAMAGE;
    }

    public Set < LootContextParam<? >> getReferencedContextParams()
    {
        return this.damage.getReferencedContextParams();
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        if (pStack.isDamageableItem())
        {
            int i = pStack.getMaxDamage();
            float f = this.add ? 1.0F - (float)pStack.getDamageValue() / (float)i : 0.0F;
            float f1 = 1.0F - Mth.clamp(this.damage.getFloat(pContext) + f, 0.0F, 1.0F);
            pStack.setDamageValue(Mth.floor(f1 * (float)i));
        }
        else
        {
            LOGGER.warn("Couldn't set damage of loot item {}", (Object)pStack);
        }

        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider pDamageValue)
    {
        return simpleBuilder((p_165441_) ->
        {
            return new SetItemDamageFunction(p_165441_, pDamageValue, false);
        });
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider pDamageValue, boolean pAdd)
    {
        return simpleBuilder((p_165438_) ->
        {
            return new SetItemDamageFunction(p_165438_, pDamageValue, pAdd);
        });
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemDamageFunction>
    {
        public void serialize(JsonObject pJson, SetItemDamageFunction pValue, JsonSerializationContext pSerializationContext)
        {
            super.serialize(pJson, pValue, pSerializationContext);
            pJson.add("damage", pSerializationContext.serialize(pValue.damage));
            pJson.addProperty("add", pValue.add);
        }

        public SetItemDamageFunction b(JsonObject p_81060_, JsonDeserializationContext p_81061_, LootItemCondition[] p_81062_)
        {
            NumberProvider numberprovider = GsonHelper.getAsObject(p_81060_, "damage", p_81061_, NumberProvider.class);
            boolean flag = GsonHelper.getAsBoolean(p_81060_, "add", false);
            return new SetItemDamageFunction(p_81062_, numberprovider, flag);
        }
    }
}
