package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootPoolEntryContainer implements ComposableEntryContainer
{
    protected final LootItemCondition[] conditions;
    private final Predicate<LootContext> compositeCondition;

    protected LootPoolEntryContainer(LootItemCondition[] pConditions)
    {
        this.conditions = pConditions;
        this.compositeCondition = LootItemConditions.a(pConditions);
    }

    public void validate(ValidationContext pValidationContext)
    {
        for (int i = 0; i < this.conditions.length; ++i)
        {
            this.conditions[i].validate(pValidationContext.forChild(".condition[" + i + "]"));
        }
    }

    protected final boolean canRun(LootContext pLootContext)
    {
        return this.compositeCondition.test(pLootContext);
    }

    public abstract LootPoolEntryType getType();

    public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T>
    {
        private final List<LootItemCondition> conditions = Lists.newArrayList();

        protected abstract T getThis();

        public T when(LootItemCondition.Builder pConditionBuilder)
        {
            this.conditions.add(pConditionBuilder.build());
            return this.getThis();
        }

        public final T unwrap()
        {
            return this.getThis();
        }

        protected LootItemCondition[] getConditions()
        {
            return this.conditions.toArray(new LootItemCondition[0]);
        }

        public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> pChildBuilder)
        {
            return new AlternativesEntry.Builder(this, pChildBuilder);
        }

        public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> pChildBuilder)
        {
            return new EntryGroup.Builder(this, pChildBuilder);
        }

        public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> pChildBuilder)
        {
            return new SequentialEntry.Builder(this, pChildBuilder);
        }

        public abstract LootPoolEntryContainer build();
    }

    public abstract static class Serializer<T extends LootPoolEntryContainer> implements net.minecraft.world.level.storage.loot.Serializer<T>
    {
        public final void serialize(JsonObject pJson, T pValue, JsonSerializationContext pSerializationContext)
        {
            if (!ArrayUtils.isEmpty((Object[])pValue.conditions))
            {
                pJson.add("conditions", pSerializationContext.serialize(pValue.conditions));
            }

            this.serializeCustom(pJson, pValue, pSerializationContext);
        }

        public final T deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext)
        {
            LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(pJson, "conditions", new LootItemCondition[0], pSerializationContext, LootItemCondition[].class);
            return this.b(pJson, pSerializationContext, alootitemcondition);
        }

        public abstract void serializeCustom(JsonObject pObject, T pContext, JsonSerializationContext pConditions);

        public abstract T b(JsonObject p_79666_, JsonDeserializationContext p_79667_, LootItemCondition[] p_79668_);
    }
}
