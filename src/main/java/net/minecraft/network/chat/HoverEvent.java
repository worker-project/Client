package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class HoverEvent
{
    static final Logger LOGGER = LogUtils.getLogger();
    private final HoverEvent.Action<?> action;
    private final Object value;

    public <T> HoverEvent(HoverEvent.Action<T> pAction, T pValue)
    {
        this.action = pAction;
        this.value = pValue;
    }

    public HoverEvent.Action<?> getAction()
    {
        return this.action;
    }

    @Nullable
    public <T> T getValue(HoverEvent.Action<T> pActionType)
    {
        return (T)(this.action == pActionType ? pActionType.cast(this.value) : null);
    }

    public boolean equals(Object p_130828_)
    {
        if (this == p_130828_)
        {
            return true;
        }
        else if (p_130828_ != null && this.getClass() == p_130828_.getClass())
        {
            HoverEvent hoverevent = (HoverEvent)p_130828_;
            return this.action == hoverevent.action && Objects.equals(this.value, hoverevent.value);
        }
        else
        {
            return false;
        }
    }

    public String toString()
    {
        return "HoverEvent{action=" + this.action + ", value='" + this.value + "'}";
    }

    public int hashCode()
    {
        int i = this.action.hashCode();
        return 31 * i + (this.value != null ? this.value.hashCode() : 0);
    }

    @Nullable
    public static HoverEvent deserialize(JsonObject pJson)
    {
        String s = GsonHelper.getAsString(pJson, "action", (String)null);

        if (s == null)
        {
            return null;
        }
        else
        {
            HoverEvent.Action<?> action = HoverEvent.Action.getByName(s);

            if (action == null)
            {
                return null;
            }
            else
            {
                JsonElement jsonelement = pJson.get("contents");

                if (jsonelement != null)
                {
                    return action.deserialize(jsonelement);
                }
                else
                {
                    Component component = Component.Serializer.fromJson(pJson.get("value"));
                    return component != null ? action.deserializeFromLegacy(component) : null;
                }
            }
        }
    }

    public JsonObject serialize()
    {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("action", this.action.getName());
        jsonobject.add("contents", this.action.serializeArg(this.value));
        return jsonobject;
    }

    public static class Action<T>
    {
        public static final HoverEvent.Action<Component> SHOW_TEXT = new HoverEvent.Action<>("show_text", true, Component.Serializer::fromJson, Component.Serializer::toJsonTree, Function.identity());
        public static final HoverEvent.Action<HoverEvent.ItemStackInfo> SHOW_ITEM = new HoverEvent.Action<>("show_item", true, HoverEvent.ItemStackInfo::create, HoverEvent.ItemStackInfo::serialize, HoverEvent.ItemStackInfo::create);
        public static final HoverEvent.Action<HoverEvent.EntityTooltipInfo> SHOW_ENTITY = new HoverEvent.Action<>("show_entity", true, HoverEvent.EntityTooltipInfo::create, HoverEvent.EntityTooltipInfo::serialize, HoverEvent.EntityTooltipInfo::create);
        private static final Map < String, HoverEvent.Action<? >> LOOKUP = Stream.of(SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY).collect(ImmutableMap.toImmutableMap(HoverEvent.Action::getName, (p_178444_) ->
        {
            return p_178444_;
        }));
        private final String name;
        private final boolean allowFromServer;
        private final Function<JsonElement, T> argDeserializer;
        private final Function<T, JsonElement> argSerializer;
        private final Function<Component, T> legacyArgDeserializer;

        public Action(String p_130842_, boolean p_130843_, Function<JsonElement, T> p_130844_, Function<T, JsonElement> p_130845_, Function<Component, T> p_130846_)
        {
            this.name = p_130842_;
            this.allowFromServer = p_130843_;
            this.argDeserializer = p_130844_;
            this.argSerializer = p_130845_;
            this.legacyArgDeserializer = p_130846_;
        }

        public boolean isAllowedFromServer()
        {
            return this.allowFromServer;
        }

        public String getName()
        {
            return this.name;
        }

        @Nullable
        public static HoverEvent.Action<?> getByName(String pCanonicalName)
        {
            return LOOKUP.get(pCanonicalName);
        }

        T cast(Object pParameter)
        {
            return (T)pParameter;
        }

        @Nullable
        public HoverEvent deserialize(JsonElement pElement)
        {
            T t = this.argDeserializer.apply(pElement);
            return t == null ? null : new HoverEvent(this, t);
        }

        @Nullable
        public HoverEvent deserializeFromLegacy(Component pComponent)
        {
            T t = this.legacyArgDeserializer.apply(pComponent);
            return t == null ? null : new HoverEvent(this, t);
        }

        public JsonElement serializeArg(Object pParameter)
        {
            return this.argSerializer.apply(this.cast(pParameter));
        }

        public String toString()
        {
            return "<action " + this.name + ">";
        }
    }

    public static class EntityTooltipInfo
    {
        public final EntityType<?> type;
        public final UUID id;
        @Nullable
        public final Component name;
        @Nullable
        private List<Component> linesCache;

        public EntityTooltipInfo(EntityType<?> pType, UUID pId, @Nullable Component pName)
        {
            this.type = pType;
            this.id = pId;
            this.name = pName;
        }

        @Nullable
        public static HoverEvent.EntityTooltipInfo create(JsonElement pElement)
        {
            if (!pElement.isJsonObject())
            {
                return null;
            }
            else
            {
                JsonObject jsonobject = pElement.getAsJsonObject();
                EntityType<?> entitytype = Registry.ENTITY_TYPE.get(new ResourceLocation(GsonHelper.getAsString(jsonobject, "type")));
                UUID uuid = UUID.fromString(GsonHelper.getAsString(jsonobject, "id"));
                Component component = Component.Serializer.fromJson(jsonobject.get("name"));
                return new HoverEvent.EntityTooltipInfo(entitytype, uuid, component);
            }
        }

        @Nullable
        public static HoverEvent.EntityTooltipInfo create(Component pElement)
        {
            try
            {
                CompoundTag compoundtag = TagParser.parseTag(pElement.getString());
                Component component = Component.Serializer.fromJson(compoundtag.getString("name"));
                EntityType<?> entitytype = Registry.ENTITY_TYPE.get(new ResourceLocation(compoundtag.getString("type")));
                UUID uuid = UUID.fromString(compoundtag.getString("id"));
                return new HoverEvent.EntityTooltipInfo(entitytype, uuid, component);
            }
            catch (Exception exception)
            {
                return null;
            }
        }

        public JsonElement serialize()
        {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("type", Registry.ENTITY_TYPE.getKey(this.type).toString());
            jsonobject.addProperty("id", this.id.toString());

            if (this.name != null)
            {
                jsonobject.add("name", Component.Serializer.toJsonTree(this.name));
            }

            return jsonobject;
        }

        public List<Component> getTooltipLines()
        {
            if (this.linesCache == null)
            {
                this.linesCache = Lists.newArrayList();

                if (this.name != null)
                {
                    this.linesCache.add(this.name);
                }

                this.linesCache.add(new TranslatableComponent("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(new TextComponent(this.id.toString()));
            }

            return this.linesCache;
        }

        public boolean equals(Object p_130886_)
        {
            if (this == p_130886_)
            {
                return true;
            }
            else if (p_130886_ != null && this.getClass() == p_130886_.getClass())
            {
                HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = (HoverEvent.EntityTooltipInfo)p_130886_;
                return this.type.equals(hoverevent$entitytooltipinfo.type) && this.id.equals(hoverevent$entitytooltipinfo.id) && Objects.equals(this.name, hoverevent$entitytooltipinfo.name);
            }
            else
            {
                return false;
            }
        }

        public int hashCode()
        {
            int i = this.type.hashCode();
            i = 31 * i + this.id.hashCode();
            return 31 * i + (this.name != null ? this.name.hashCode() : 0);
        }
    }

    public static class ItemStackInfo
    {
        private final Item item;
        private final int count;
        @Nullable
        private final CompoundTag tag;
        @Nullable
        private ItemStack itemStack;

        ItemStackInfo(Item pItem, int pCount, @Nullable CompoundTag pTag)
        {
            this.item = pItem;
            this.count = pCount;
            this.tag = pTag;
        }

        public ItemStackInfo(ItemStack pStack)
        {
            this(pStack.getItem(), pStack.getCount(), pStack.getTag() != null ? pStack.getTag().copy() : null);
        }

        public boolean equals(Object p_130911_)
        {
            if (this == p_130911_)
            {
                return true;
            }
            else if (p_130911_ != null && this.getClass() == p_130911_.getClass())
            {
                HoverEvent.ItemStackInfo hoverevent$itemstackinfo = (HoverEvent.ItemStackInfo)p_130911_;
                return this.count == hoverevent$itemstackinfo.count && this.item.equals(hoverevent$itemstackinfo.item) && Objects.equals(this.tag, hoverevent$itemstackinfo.tag);
            }
            else
            {
                return false;
            }
        }

        public int hashCode()
        {
            int i = this.item.hashCode();
            i = 31 * i + this.count;
            return 31 * i + (this.tag != null ? this.tag.hashCode() : 0);
        }

        public ItemStack getItemStack()
        {
            if (this.itemStack == null)
            {
                this.itemStack = new ItemStack(this.item, this.count);

                if (this.tag != null)
                {
                    this.itemStack.setTag(this.tag);
                }
            }

            return this.itemStack;
        }

        private static HoverEvent.ItemStackInfo create(JsonElement pElement)
        {
            if (pElement.isJsonPrimitive())
            {
                return new HoverEvent.ItemStackInfo(Registry.ITEM.get(new ResourceLocation(pElement.getAsString())), 1, (CompoundTag)null);
            }
            else
            {
                JsonObject jsonobject = GsonHelper.convertToJsonObject(pElement, "item");
                Item item = Registry.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonobject, "id")));
                int i = GsonHelper.getAsInt(jsonobject, "count", 1);

                if (jsonobject.has("tag"))
                {
                    String s = GsonHelper.getAsString(jsonobject, "tag");

                    try
                    {
                        CompoundTag compoundtag = TagParser.parseTag(s);
                        return new HoverEvent.ItemStackInfo(item, i, compoundtag);
                    }
                    catch (CommandSyntaxException commandsyntaxexception)
                    {
                        HoverEvent.LOGGER.warn("Failed to parse tag: {}", s, commandsyntaxexception);
                    }
                }

                return new HoverEvent.ItemStackInfo(item, i, (CompoundTag)null);
            }
        }

        @Nullable
        private static HoverEvent.ItemStackInfo create(Component pElement)
        {
            try
            {
                CompoundTag compoundtag = TagParser.parseTag(pElement.getString());
                return new HoverEvent.ItemStackInfo(ItemStack.of(compoundtag));
            }
            catch (CommandSyntaxException commandsyntaxexception)
            {
                HoverEvent.LOGGER.warn("Failed to parse item tag: {}", pElement, commandsyntaxexception);
                return null;
            }
        }

        private JsonElement serialize()
        {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("id", Registry.ITEM.getKey(this.item).toString());

            if (this.count != 1)
            {
                jsonobject.addProperty("count", this.count);
            }

            if (this.tag != null)
            {
                jsonobject.addProperty("tag", this.tag.toString());
            }

            return jsonobject;
        }
    }
}
