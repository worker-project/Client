package net.minecraft.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ResourceLocation implements Comparable<ResourceLocation>
{
    public static final Codec<ResourceLocation> CODEC = Codec.STRING.comapFlatMap(ResourceLocation::read, ResourceLocation::toString).stable();
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(new TranslatableComponent("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    protected final String namespace;
    protected final String path;

    protected ResourceLocation(String[] pLocation)
    {
        this.namespace = StringUtils.isEmpty(pLocation[0]) ? "minecraft" : pLocation[0];
        this.path = pLocation[1];

        if (this.path.equals("DUMMY"))
        {
            if (!isValidNamespace(this.namespace))
            {
                throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + this.namespace + ":" + this.path);
            }
            else if (!isValidPath(this.path))
            {
                throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + this.namespace + ":" + this.path);
            }
        }
    }

    public ResourceLocation(String pLocation)
    {
        this(decompose(pLocation, ':'));
    }

    public ResourceLocation(String pNamespace, String pPath)
    {
        this(new String[] {pNamespace, pPath});
    }

    public static ResourceLocation of(String pLocation, char pSeparator)
    {
        return new ResourceLocation(decompose(pLocation, pSeparator));
    }

    @Nullable
    public static ResourceLocation tryParse(String pLocation)
    {
        try
        {
            return new ResourceLocation(pLocation);
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return null;
        }
    }

    protected static String[] decompose(String pLocation, char pSeparator)
    {
        String[] astring = new String[] {"minecraft", pLocation};
        int i = pLocation.indexOf(pSeparator);

        if (i >= 0)
        {
            astring[1] = pLocation.substring(i + 1, pLocation.length());

            if (i >= 1)
            {
                astring[0] = pLocation.substring(0, i);
            }
        }

        return astring;
    }

    public static DataResult<ResourceLocation> read(String pReader)
    {
        try
        {
            return DataResult.success(new ResourceLocation(pReader));
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return DataResult.error("Not a valid resource location: " + pReader + " " + resourcelocationexception.getMessage());
        }
    }

    public String getPath()
    {
        return this.path;
    }

    public String getNamespace()
    {
        return this.namespace;
    }

    public String toString()
    {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object pOther)
    {
        if (this == pOther)
        {
            return true;
        }
        else if (!(pOther instanceof ResourceLocation))
        {
            return false;
        }
        else
        {
            ResourceLocation resourcelocation = (ResourceLocation)pOther;
            return this.namespace.equals(resourcelocation.namespace) && this.path.equals(resourcelocation.path);
        }
    }

    public int hashCode()
    {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    public int compareTo(ResourceLocation pOther)
    {
        int i = this.path.compareTo(pOther.path);

        if (i == 0)
        {
            i = this.namespace.compareTo(pOther.namespace);
        }

        return i;
    }

    public String toDebugFileName()
    {
        return this.toString().replace('/', '_').replace(':', '_');
    }

    public static ResourceLocation read(StringReader pReader) throws CommandSyntaxException
    {
        int i = pReader.getCursor();

        while (pReader.canRead() && isAllowedInResourceLocation(pReader.peek()))
        {
            pReader.skip();
        }

        String s = pReader.getString().substring(i, pReader.getCursor());

        try
        {
            return new ResourceLocation(s);
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            pReader.setCursor(i);
            throw ERROR_INVALID.createWithContext(pReader);
        }
    }

    public static boolean isAllowedInResourceLocation(char pCharacter)
    {
        return pCharacter >= '0' && pCharacter <= '9' || pCharacter >= 'a' && pCharacter <= 'z' || pCharacter == '_' || pCharacter == ':' || pCharacter == '/' || pCharacter == '.' || pCharacter == '-';
    }

    private static boolean isValidPath(String pPath)
    {
        for (int i = 0; i < pPath.length(); ++i)
        {
            if (!validPathChar(pPath.charAt(i)))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidNamespace(String pNamespace)
    {
        for (int i = 0; i < pNamespace.length(); ++i)
        {
            if (!validNamespaceChar(pNamespace.charAt(i)))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean validPathChar(char pPathChar)
    {
        return pPathChar == '_' || pPathChar == '-' || pPathChar >= 'a' && pPathChar <= 'z' || pPathChar >= '0' && pPathChar <= '9' || pPathChar == '/' || pPathChar == '.';
    }

    private static boolean validNamespaceChar(char pNamespaceChar)
    {
        return pNamespaceChar == '_' || pNamespaceChar == '-' || pNamespaceChar >= 'a' && pNamespaceChar <= 'z' || pNamespaceChar >= '0' && pNamespaceChar <= '9' || pNamespaceChar == '.';
    }

    public static boolean isValidResourceLocation(String pLocation)
    {
        String[] astring = decompose(pLocation, ':');
        return isValidNamespace(StringUtils.isEmpty(astring[0]) ? "minecraft" : astring[0]) && isValidPath(astring[1]);
    }

    public int compareNamespaced(ResourceLocation o)
    {
        int i = this.namespace.compareTo(o.namespace);
        return i != 0 ? i : this.path.compareTo(o.path);
    }

    public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation>
    {
        public ResourceLocation deserialize(JsonElement pJson, Type pTypeOfT, JsonDeserializationContext pContext) throws JsonParseException
        {
            return new ResourceLocation(GsonHelper.convertToString(pJson, "location"));
        }

        public JsonElement serialize(ResourceLocation pResourceLocation, Type pTypeOfT, JsonSerializationContext pContext)
        {
            return new JsonPrimitive(pResourceLocation.toString());
        }
    }
}
