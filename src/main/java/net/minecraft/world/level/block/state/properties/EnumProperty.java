package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;

public class EnumProperty<T extends Enum<T> & StringRepresentable> extends Property<T>
{
    private final ImmutableSet<T> values;
    private final Map<String, T> names = Maps.newHashMap();

    protected EnumProperty(String pName, Class<T> pClazz, Collection<T> pValues)
    {
        super(pName, pClazz);
        this.values = ImmutableSet.copyOf(pValues);

        for (T t : pValues)
        {
            String s = t.getSerializedName();

            if (this.names.containsKey(s))
            {
                throw new IllegalArgumentException("Multiple values have the same name '" + s + "'");
            }

            this.names.put(s, t);
        }
    }

    public Collection<T> getPossibleValues()
    {
        return this.values;
    }

    public Optional<T> getValue(String pValue)
    {
        return Optional.ofNullable(this.names.get(pValue));
    }

    public String getName(T pValue)
    {
        return pValue.getSerializedName();
    }

    public boolean equals(Object pOther)
    {
        if (this == pOther)
        {
            return true;
        }
        else if (pOther instanceof EnumProperty && super.equals(pOther))
        {
            EnumProperty<?> enumproperty = (EnumProperty)pOther;
            return this.values.equals(enumproperty.values) && this.names.equals(enumproperty.names);
        }
        else
        {
            return false;
        }
    }

    public int generateHashCode()
    {
        int i = super.generateHashCode();
        i = 31 * i + this.values.hashCode();
        return 31 * i + this.names.hashCode();
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String pName, Class<T> pClazz)
    {
        return create(pName, pClazz, (p_187560_) ->
        {
            return true;
        });
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String pName, Class<T> pClazz, Predicate<T> pValues)
    {
        return create(pName, pClazz, Arrays.<T>stream(pClazz.getEnumConstants()).filter(pValues).collect(Collectors.toList()));
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> a(String p_61599_, Class<T> p_61600_, T... p_61601_)
    {
        return create(p_61599_, p_61600_, Lists.newArrayList(p_61601_));
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String pName, Class<T> pClazz, Collection<T> pValues)
    {
        return new EnumProperty<>(pName, pClazz, pValues);
    }
}
