package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class StatType<T> implements Iterable<Stat<T>>
{
    private final Registry<T> registry;
    private final Map<T, Stat<T>> map = new IdentityHashMap<>();
    @Nullable
    private Component displayName;

    public StatType(Registry<T> pRegistry)
    {
        this.registry = pRegistry;
    }

    public boolean contains(T pValue)
    {
        return this.map.containsKey(pValue);
    }

    public Stat<T> get(T pValue, StatFormatter pFormatter)
    {
        return this.map.computeIfAbsent(pValue, (p_12896_) ->
        {
            return new Stat<>(this, p_12896_, pFormatter);
        });
    }

    public Registry<T> getRegistry()
    {
        return this.registry;
    }

    public Iterator<Stat<T>> iterator()
    {
        return this.map.values().iterator();
    }

    public Stat<T> get(T pValue)
    {
        return this.get(pValue, StatFormatter.DEFAULT);
    }

    public String getTranslationKey()
    {
        return "stat_type." + Registry.STAT_TYPE.getKey(this).toString().replace(':', '.');
    }

    public Component getDisplayName()
    {
        if (this.displayName == null)
        {
            this.displayName = new TranslatableComponent(this.getTranslationKey());
        }

        return this.displayName;
    }
}
