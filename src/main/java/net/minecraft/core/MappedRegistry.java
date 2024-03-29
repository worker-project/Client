package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList<>(256);
    private final Object2IntMap<T> toId = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), (p_194539_) ->
    {
        p_194539_.defaultReturnValue(-1);
    });
    private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap<>();
    private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<>();
    private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<>();
    private final Map<T, Lifecycle> lifecycles = new IdentityHashMap<>();
    private Lifecycle elementsLifecycle;
    private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap<>();
    private boolean frozen;
    @Nullable
    private final Function<T, Holder.Reference<T>> customHolderProvider;
    @Nullable
    private Map<T, Holder.Reference<T>> intrusiveHolderCache;
    @Nullable
    private List<Holder.Reference<T>> holdersInOrder;
    private int nextId;

    public MappedRegistry(ResourceKey <? extends Registry<T >> p_205849_, Lifecycle p_205850_, @Nullable Function<T, Holder.Reference<T>> p_205851_)
    {
        super(p_205849_, p_205850_);
        this.elementsLifecycle = p_205850_;
        this.customHolderProvider = p_205851_;

        if (p_205851_ != null)
        {
            this.intrusiveHolderCache = new IdentityHashMap<>();
        }
    }

    private List<Holder.Reference<T>> holdersInOrder()
    {
        if (this.holdersInOrder == null)
        {
            this.holdersInOrder = this.byId.stream().filter(Objects::nonNull).toList();
        }

        return this.holdersInOrder;
    }

    private void validateWrite(ResourceKey<T> p_205922_)
    {
        if (this.frozen)
        {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + p_205922_ + ")");
        }
    }

    public Holder<T> registerMapping(int pId, ResourceKey<T> pKey, T pValue, Lifecycle pLifecycle)
    {
        return this.registerMapping(pId, pKey, pValue, pLifecycle, true);
    }

    private Holder<T> registerMapping(int pId, ResourceKey<T> pKey, T pValue, Lifecycle pLifecycle, boolean pLogDuplicateKeys)
    {
        this.validateWrite(pKey);
        Validate.notNull(pKey);
        Validate.notNull(pValue);
        this.byId.size(Math.max(this.byId.size(), pId + 1));
        this.toId.put(pValue, pId);
        this.holdersInOrder = null;

        if (pLogDuplicateKeys && this.byKey.containsKey(pKey))
        {
            Util.logAndPauseIfInIde("Adding duplicate key '" + pKey + "' to registry");
        }

        if (this.byValue.containsKey(pValue))
        {
            Util.logAndPauseIfInIde("Adding duplicate value '" + pValue + "' to registry");
        }

        this.lifecycles.put(pValue, pLifecycle);
        this.elementsLifecycle = this.elementsLifecycle.add(pLifecycle);

        if (this.nextId <= pId)
        {
            this.nextId = pId + 1;
        }

        Holder.Reference<T> reference;

        if (this.customHolderProvider != null)
        {
            reference = this.customHolderProvider.apply(pValue);
            Holder.Reference<T> reference1 = this.byKey.put(pKey, reference);

            if (reference1 != null && reference1 != reference)
            {
                throw new IllegalStateException("Invalid holder present for key " + pKey);
            }
        }
        else
        {
            reference = this.byKey.computeIfAbsent(pKey, (p_205927_) ->
            {
                return Holder.Reference.createStandAlone(this, p_205927_);
            });
        }

        this.byLocation.put(pKey.location(), reference);
        this.byValue.put(pValue, reference);
        reference.bind(pKey, pValue);
        this.byId.set(pId, reference);
        return reference;
    }

    public Holder<T> register(ResourceKey<T> pKey, T pValue, Lifecycle pLifecycle)
    {
        return this.registerMapping(this.nextId, pKey, pValue, pLifecycle);
    }

    public Holder<T> registerOrOverride(OptionalInt pId, ResourceKey<T> pKey, T pValue, Lifecycle pLifecycle)
    {
        this.validateWrite(pKey);
        Validate.notNull(pKey);
        Validate.notNull(pValue);
        Holder<T> holder = this.byKey.get(pKey);
        T t = (T)(holder != null && holder.isBound() ? holder.value() : null);
        int i;

        if (t == null)
        {
            i = pId.orElse(this.nextId);
        }
        else
        {
            i = this.toId.getInt(t);

            if (pId.isPresent() && pId.getAsInt() != i)
            {
                throw new IllegalStateException("ID mismatch");
            }

            this.lifecycles.remove(t);
            this.toId.removeInt(t);
            this.byValue.remove(t);
        }

        return this.registerMapping(i, pKey, pValue, pLifecycle, false);
    }

    @Nullable
    public ResourceLocation getKey(T pValue)
    {
        Holder.Reference<T> reference = this.byValue.get(pValue);
        return reference != null ? reference.key().location() : null;
    }

    public Optional<ResourceKey<T>> getResourceKey(T pValue)
    {
        return Optional.ofNullable(this.byValue.get(pValue)).map(Holder.Reference::key);
    }

    public int getId(@Nullable T pValue)
    {
        return this.toId.getInt(pValue);
    }

    @Nullable
    public T get(@Nullable ResourceKey<T> pKey)
    {
        return getValueFromNullable(this.byKey.get(pKey));
    }

    @Nullable
    public T byId(int pId)
    {
        return (T)(pId >= 0 && pId < this.byId.size() ? getValueFromNullable(this.byId.get(pId)) : null);
    }

    public Optional<Holder<T>> getHolder(int p_205907_)
    {
        return p_205907_ >= 0 && p_205907_ < this.byId.size() ? Optional.ofNullable(this.byId.get(p_205907_)) : Optional.empty();
    }

    public Optional<Holder<T>> getHolder(ResourceKey<T> p_205905_)
    {
        return Optional.ofNullable(this.byKey.get(p_205905_));
    }

    public Holder<T> getOrCreateHolder(ResourceKey<T> p_205913_)
    {
        return this.byKey.computeIfAbsent(p_205913_, (p_205924_) ->
        {
            if (this.customHolderProvider != null)
            {
                throw new IllegalStateException("This registry can't create new holders without value");
            }
            else {
                this.validateWrite(p_205924_);
                return Holder.Reference.createStandAlone(this, p_205924_);
            }
        });
    }

    public int size()
    {
        return this.byKey.size();
    }

    public Lifecycle lifecycle(T pValue)
    {
        return this.lifecycles.get(pValue);
    }

    public Lifecycle elementsLifecycle()
    {
        return this.elementsLifecycle;
    }

    public Iterator<T> iterator()
    {
        return Iterators.transform(this.holdersInOrder().iterator(), Holder::value);
    }

    @Nullable
    public T get(@Nullable ResourceLocation pKey)
    {
        Holder.Reference<T> reference = this.byLocation.get(pKey);
        return getValueFromNullable(reference);
    }

    @Nullable
    private static <T> T getValueFromNullable(@Nullable Holder.Reference<T> p_205866_)
    {
        return (T)(p_205866_ != null ? p_205866_.value() : null);
    }

    public Set<ResourceLocation> keySet()
    {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    public Set<Entry<ResourceKey<T>, T>> entrySet()
    {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
    }

    public Stream<Holder.Reference<T>> holders()
    {
        return this.holdersInOrder().stream();
    }

    public boolean isKnownTagName(TagKey<T> p_205864_)
    {
        return this.tags.containsKey(p_205864_);
    }

    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags()
    {
        return this.tags.entrySet().stream().map((p_211060_) ->
        {
            return Pair.of(p_211060_.getKey(), p_211060_.getValue());
        });
    }

    public HolderSet.Named<T> getOrCreateTag(TagKey<T> p_205895_)
    {
        HolderSet.Named<T> named = this.tags.get(p_205895_);

        if (named == null)
        {
            named = this.createTag(p_205895_);
            Map<TagKey<T>, HolderSet.Named<T>> map = new IdentityHashMap<>(this.tags);
            map.put(p_205895_, named);
            this.tags = map;
        }

        return named;
    }

    private HolderSet.Named<T> createTag(TagKey<T> p_211068_)
    {
        return new HolderSet.Named<>(this, p_211068_);
    }

    public Stream<TagKey<T>> getTagNames()
    {
        return this.tags.keySet().stream();
    }

    public boolean isEmpty()
    {
        return this.byKey.isEmpty();
    }

    public Optional<Holder<T>> getRandom(Random pRandom)
    {
        return Util.getRandomSafe(this.holdersInOrder(), pRandom).map(Holder::hackyErase);
    }

    public boolean containsKey(ResourceLocation pKey)
    {
        return this.byLocation.containsKey(pKey);
    }

    public boolean containsKey(ResourceKey<T> pKey)
    {
        return this.byKey.containsKey(pKey);
    }

    public Registry<T> freeze()
    {
        this.frozen = true;
        List<ResourceLocation> list = this.byKey.entrySet().stream().filter((p_211055_) ->
        {
            return !p_211055_.getValue().isBound();
        }).map((p_211794_) ->
        {
            return p_211794_.getKey().location();
        }).sorted().toList();

        if (!list.isEmpty())
        {
            throw new IllegalStateException("Unbound values in registry " + this.key() + ": " + list);
        }
        else
        {
            if (this.intrusiveHolderCache != null)
            {
                List<Holder.Reference<T>> list1 = this.intrusiveHolderCache.values().stream().filter((p_211809_) ->
                {
                    return !p_211809_.isBound();
                }).toList();

                if (!list1.isEmpty())
                {
                    throw new IllegalStateException("Some intrusive holders were not added to registry: " + list1);
                }

                this.intrusiveHolderCache = null;
            }

            return this;
        }
    }

    public Holder.Reference<T> createIntrusiveHolder(T p_205915_)
    {
        if (this.customHolderProvider == null)
        {
            throw new IllegalStateException("This registry can't create intrusive holders");
        }
        else if (!this.frozen && this.intrusiveHolderCache != null)
        {
            return this.intrusiveHolderCache.computeIfAbsent(p_205915_, (p_211813_) ->
            {
                return Holder.Reference.createIntrusive(this, p_211813_);
            });
        }
        else
        {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    public Optional<HolderSet.Named<T>> getTag(TagKey<T> p_205909_)
    {
        return Optional.ofNullable(this.tags.get(p_205909_));
    }

    public void bindTags(Map<TagKey<T>, List<Holder<T>>> p_205875_)
    {
        Map<Holder.Reference<T>, List<TagKey<T>>> map = new IdentityHashMap<>();
        this.byKey.values().forEach((p_211801_) ->
        {
            map.put(p_211801_, new ArrayList<>());
        });
        p_205875_.forEach((p_211806_, p_211807_) ->
        {
            for (Holder<T> holder : p_211807_)
            {
                if (!holder.isValidInRegistry(this))
                {
                    throw new IllegalStateException("Can't create named set " + p_211806_ + " containing value " + holder + " from outside registry " + this);
                }

                if (!(holder instanceof Holder.Reference))
                {
                    throw new IllegalStateException("Found direct holder " + holder + " value in tag " + p_211806_);
                }

                Holder.Reference<T> reference = (Holder.Reference)holder;
                map.get(reference).add(p_211806_);
            }
        });
        Set<TagKey<T>> set = Sets.difference(this.tags.keySet(), p_205875_.keySet());

        if (!set.isEmpty())
        {
            LOGGER.warn("Not all defined tags for registry {} are present in data pack: {}", this.key(), set.stream().map((p_211811_) ->
            {
                return p_211811_.location().toString();
            }).sorted().collect(Collectors.joining(", ")));
        }

        Map<TagKey<T>, HolderSet.Named<T>> map1 = new IdentityHashMap<>(this.tags);
        p_205875_.forEach((p_211797_, p_211798_) ->
        {
            map1.computeIfAbsent(p_211797_, this::createTag).bind(p_211798_);
        });
        map.forEach(Holder.Reference::bindTags);
        this.tags = map1;
    }

    public void resetTags()
    {
        this.tags.values().forEach((p_211792_) ->
        {
            p_211792_.bind(List.of());
        });
        this.byKey.values().forEach((p_211803_) ->
        {
            p_211803_.bindTags(Set.of());
        });
    }
}
