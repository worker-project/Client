package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction
{
    final ResourceLocation name;
    final long seed;
    final BlockEntityType<?> type;

    SetContainerLootTable(LootItemCondition[] p_193045_, ResourceLocation p_193046_, long p_193047_, BlockEntityType<?> p_193048_)
    {
        super(p_193045_);
        this.name = p_193046_;
        this.seed = p_193047_;
        this.type = p_193048_;
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.SET_LOOT_TABLE;
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        if (pStack.isEmpty())
        {
            return pStack;
        }
        else
        {
            CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);

            if (compoundtag == null)
            {
                compoundtag = new CompoundTag();
            }

            compoundtag.putString("LootTable", this.name.toString());

            if (this.seed != 0L)
            {
                compoundtag.putLong("LootTableSeed", this.seed);
            }

            BlockItem.setBlockEntityData(pStack, this.type, compoundtag);
            return pStack;
        }
    }

    public void validate(ValidationContext pContext)
    {
        if (pContext.hasVisitedTable(this.name))
        {
            pContext.reportProblem("Table " + this.name + " is recursively called");
        }
        else
        {
            super.validate(pContext);
            LootTable loottable = pContext.resolveLootTable(this.name);

            if (loottable == null)
            {
                pContext.reportProblem("Unknown loot table called " + this.name);
            }
            else
            {
                loottable.validate(pContext.enterTable("->{" + this.name + "}", this.name));
            }
        }
    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> p_193050_, ResourceLocation p_193051_)
    {
        return simpleBuilder((p_193064_) ->
        {
            return new SetContainerLootTable(p_193064_, p_193051_, 0L, p_193050_);
        });
    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> p_193053_, ResourceLocation p_193054_, long p_193055_)
    {
        return simpleBuilder((p_193060_) ->
        {
            return new SetContainerLootTable(p_193060_, p_193054_, p_193055_, p_193053_);
        });
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable>
    {
        public void serialize(JsonObject pJson, SetContainerLootTable pValue, JsonSerializationContext pSerializationContext)
        {
            super.serialize(pJson, pValue, pSerializationContext);
            pJson.addProperty("name", pValue.name.toString());
            pJson.addProperty("type", Registry.BLOCK_ENTITY_TYPE.getKey(pValue.type).toString());

            if (pValue.seed != 0L)
            {
                pJson.addProperty("seed", pValue.seed);
            }
        }

        public SetContainerLootTable b(JsonObject p_80978_, JsonDeserializationContext p_80979_, LootItemCondition[] p_80980_)
        {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_80978_, "name"));
            long i = GsonHelper.getAsLong(p_80978_, "seed", 0L);
            ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(p_80978_, "type"));
            BlockEntityType<?> blockentitytype = Registry.BLOCK_ENTITY_TYPE.getOptional(resourcelocation1).orElseThrow(() ->
            {
                return new JsonSyntaxException("Unknown block entity type id '" + resourcelocation1 + "'");
            });
            return new SetContainerLootTable(p_80980_, resourcelocation, i, blockentitytype);
        }
    }
}
