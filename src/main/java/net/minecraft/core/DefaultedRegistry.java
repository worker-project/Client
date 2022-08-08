package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DefaultedRegistry<T> extends MappedRegistry<T>
{
    private final ResourceLocation defaultKey;
    private Holder<T> defaultValue;

    public DefaultedRegistry(String p_205693_, ResourceKey <? extends Registry<T >> p_205694_, Lifecycle p_205695_, @Nullable Function<T, Holder.Reference<T>> p_205696_)
    {
        super(p_205694_, p_205695_, p_205696_);
        this.defaultKey = new ResourceLocation(p_205693_);
    }

    public Holder<T> registerMapping(int pId, ResourceKey<T> pKey, T pValue, Lifecycle pLifecycle)
    {
        Holder<T> holder = super.registerMapping(pId, pKey, pValue, pLifecycle);

        if (this.defaultKey.equals(pKey.location()))
        {
            this.defaultValue = holder;
        }

        return holder;
    }

    public int getId(@Nullable T pValue)
    {
        int i = super.getId(pValue);
        return i == -1 ? super.getId(this.defaultValue.value()) : i;
    }

    @Nonnull
    public ResourceLocation getKey(T pValue)
    {
        ResourceLocation resourcelocation = super.getKey(pValue);
        return resourcelocation == null ? this.defaultKey : resourcelocation;
    }

    @Nonnull
    public T get(@Nullable ResourceLocation pName)
    {
        T t = super.get(pName);
        return (T)(t == null ? this.defaultValue.value() : t);
    }

    public Optional<T> getOptional(@Nullable ResourceLocation pName)
    {
        return Optional.ofNullable(super.get(pName));
    }

    @Nonnull
    public T byId(int pId)
    {
        T t = super.byId(pId);
        return (T)(t == null ? this.defaultValue.value() : t);
    }

    public Optional<Holder<T>> getRandom(Random pRandom)
    {
        return super.getRandom(pRandom).or(() ->
        {
            return Optional.of(this.defaultValue);
        });
    }

    public ResourceLocation getDefaultKey()
    {
        return this.defaultKey;
    }
}
