package net.minecraft.world.entity.ai.attributes;

import com.mojang.logging.LogUtils;
import io.netty.util.internal.ThreadLocalRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class AttributeModifier
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final Supplier<String> nameGetter;
    private final UUID id;

    public AttributeModifier(String pName, double pAmount, AttributeModifier.Operation p_22198_)
    {
        this(Mth.createInsecureUUID(ThreadLocalRandom.current()), () ->
        {
            return pName;
        }, pAmount, p_22198_);
    }

    public AttributeModifier(UUID pId, String pName, double pAmount, AttributeModifier.Operation p_22203_)
    {
        this(pId, () ->
        {
            return pName;
        }, pAmount, p_22203_);
    }

    public AttributeModifier(UUID pId, Supplier<String> pName, double pAmount, AttributeModifier.Operation p_22208_)
    {
        this.id = pId;
        this.nameGetter = pName;
        this.amount = pAmount;
        this.operation = p_22208_;
    }

    public UUID getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.nameGetter.get();
    }

    public AttributeModifier.Operation getOperation()
    {
        return this.operation;
    }

    public double getAmount()
    {
        return this.amount;
    }

    public boolean equals(Object pOther)
    {
        if (this == pOther)
        {
            return true;
        }
        else if (pOther != null && this.getClass() == pOther.getClass())
        {
            AttributeModifier attributemodifier = (AttributeModifier)pOther;
            return Objects.equals(this.id, attributemodifier.id);
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return this.id.hashCode();
    }

    public String toString()
    {
        return "AttributeModifier{amount=" + this.amount + ", operation=" + this.operation + ", name='" + (String)this.nameGetter.get() + "', id=" + this.id + "}";
    }

    public CompoundTag save()
    {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("Name", this.getName());
        compoundtag.putDouble("Amount", this.amount);
        compoundtag.putInt("Operation", this.operation.toValue());
        compoundtag.putUUID("UUID", this.id);
        return compoundtag;
    }

    @Nullable
    public static AttributeModifier load(CompoundTag pNbt)
    {
        try
        {
            UUID uuid = pNbt.getUUID("UUID");
            AttributeModifier.Operation attributemodifier$operation = AttributeModifier.Operation.fromValue(pNbt.getInt("Operation"));
            return new AttributeModifier(uuid, pNbt.getString("Name"), pNbt.getDouble("Amount"), attributemodifier$operation);
        }
        catch (Exception exception)
        {
            LOGGER.warn("Unable to create attribute: {}", (Object)exception.getMessage());
            return null;
        }
    }

    public static enum Operation
    {
        ADDITION(0),
        MULTIPLY_BASE(1),
        MULTIPLY_TOTAL(2);

        private static final AttributeModifier.Operation[] OPERATIONS = new AttributeModifier.Operation[]{ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
        private final int value;

        private Operation(int p_22234_)
        {
            this.value = p_22234_;
        }

        public int toValue()
        {
            return this.value;
        }

        public static AttributeModifier.Operation fromValue(int pId)
        {
            if (pId >= 0 && pId < OPERATIONS.length)
            {
                return OPERATIONS[pId];
            }
            else
            {
                throw new IllegalArgumentException("No operation with value " + pId);
            }
        }
    }
}
