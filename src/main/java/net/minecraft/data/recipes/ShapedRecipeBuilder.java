package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class ShapedRecipeBuilder implements RecipeBuilder
{
    private final Item result;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;

    public ShapedRecipeBuilder(ItemLike pResult, int pCount)
    {
        this.result = pResult.asItem();
        this.count = pCount;
    }

    public static ShapedRecipeBuilder shaped(ItemLike pResult)
    {
        return shaped(pResult, 1);
    }

    public static ShapedRecipeBuilder shaped(ItemLike pResult, int pCount)
    {
        return new ShapedRecipeBuilder(pResult, pCount);
    }

    public ShapedRecipeBuilder define(Character pSymbol, TagKey<Item> pTag)
    {
        return this.define(pSymbol, Ingredient.of(pTag));
    }

    public ShapedRecipeBuilder define(Character pSymbol, ItemLike pTag)
    {
        return this.define(pSymbol, Ingredient.a(pTag));
    }

    public ShapedRecipeBuilder define(Character pSymbol, Ingredient pTag)
    {
        if (this.key.containsKey(pSymbol))
        {
            throw new IllegalArgumentException("Symbol '" + pSymbol + "' is already defined!");
        }
        else if (pSymbol == ' ')
        {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }
        else
        {
            this.key.put(pSymbol, pTag);
            return this;
        }
    }

    public ShapedRecipeBuilder pattern(String pPattern)
    {
        if (!this.rows.isEmpty() && pPattern.length() != this.rows.get(0).length())
        {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        }
        else
        {
            this.rows.add(pPattern);
            return this;
        }
    }

    public ShapedRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger)
    {
        this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
        return this;
    }

    public ShapedRecipeBuilder group(@Nullable String pGroupName)
    {
        this.group = pGroupName;
        return this;
    }

    public Item getResult()
    {
        return this.result;
    }

    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId)
    {
        this.ensureValid(pRecipeId);
        this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.OR);
        pFinishedRecipeConsumer.accept(new ShapedRecipeBuilder.Result(pRecipeId, this.result, this.count, this.group == null ? "" : this.group, this.rows, this.key, this.advancement, new ResourceLocation(pRecipeId.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + pRecipeId.getPath())));
    }

    private void ensureValid(ResourceLocation pId)
    {
        if (this.rows.isEmpty())
        {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + pId + "!");
        }
        else
        {
            Set<Character> set = Sets.newHashSet(this.key.keySet());
            set.remove(' ');

            for (String s : this.rows)
            {
                for (int i = 0; i < s.length(); ++i)
                {
                    char c0 = s.charAt(i);

                    if (!this.key.containsKey(c0) && c0 != ' ')
                    {
                        throw new IllegalStateException("Pattern in recipe " + pId + " uses undefined symbol '" + c0 + "'");
                    }

                    set.remove(c0);
                }
            }

            if (!set.isEmpty())
            {
                throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + pId);
            }
            else if (this.rows.size() == 1 && this.rows.get(0).length() == 1)
            {
                throw new IllegalStateException("Shaped recipe " + pId + " only takes in a single item - should it be a shapeless recipe instead?");
            }
            else if (this.advancement.getCriteria().isEmpty())
            {
                throw new IllegalStateException("No way of obtaining recipe " + pId);
            }
        }
    }

    static class Result implements FinishedRecipe
    {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String group;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation pId, Item pResult, int pCount, String pGroup, List<String> pPattern, Map<Character, Ingredient> pKey, Advancement.Builder pAdvancement, ResourceLocation pAdvancementId)
        {
            this.id = pId;
            this.result = pResult;
            this.count = pCount;
            this.group = pGroup;
            this.pattern = pPattern;
            this.key = pKey;
            this.advancement = pAdvancement;
            this.advancementId = pAdvancementId;
        }

        public void serializeRecipeData(JsonObject pJson)
        {
            if (!this.group.isEmpty())
            {
                pJson.addProperty("group", this.group);
            }

            JsonArray jsonarray = new JsonArray();

            for (String s : this.pattern)
            {
                jsonarray.add(s);
            }

            pJson.add("pattern", jsonarray);
            JsonObject jsonobject = new JsonObject();

            for (Entry<Character, Ingredient> entry : this.key.entrySet())
            {
                jsonobject.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }

            pJson.add("key", jsonobject);
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("item", Registry.ITEM.getKey(this.result).toString());

            if (this.count > 1)
            {
                jsonobject1.addProperty("count", this.count);
            }

            pJson.add("result", jsonobject1);
        }

        public RecipeSerializer<?> getType()
        {
            return RecipeSerializer.SHAPED_RECIPE;
        }

        public ResourceLocation getId()
        {
            return this.id;
        }

        @Nullable
        public JsonObject serializeAdvancement()
        {
            return this.advancement.serializeToJson();
        }

        @Nullable
        public ResourceLocation getAdvancementId()
        {
            return this.advancementId;
        }
    }
}
