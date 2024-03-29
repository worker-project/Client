package net.minecraft.client.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public class DefaultClientPackResources extends VanillaPackResources
{
    private final AssetIndex assetIndex;

    public DefaultClientPackResources(PackMetadataSection pPackMetadata, AssetIndex pAssetIndex)
    {
        super(pPackMetadata, "minecraft");
        this.assetIndex = pAssetIndex;
    }

    @Nullable
    protected InputStream getResourceAsStream(PackType pType, ResourceLocation pLocation)
    {
        if (pType == PackType.CLIENT_RESOURCES)
        {
            File file1 = this.assetIndex.getFile(pLocation);

            if (file1 != null && file1.exists())
            {
                try
                {
                    return new FileInputStream(file1);
                }
                catch (FileNotFoundException filenotfoundexception)
                {
                }
            }
        }

        return super.getResourceAsStream(pType, pLocation);
    }

    public boolean hasResource(PackType pType, ResourceLocation pLocation)
    {
        if (pType == PackType.CLIENT_RESOURCES)
        {
            File file1 = this.assetIndex.getFile(pLocation);

            if (file1 != null && file1.exists())
            {
                return true;
            }
        }

        return super.hasResource(pType, pLocation);
    }

    @Nullable
    protected InputStream getResourceAsStream(String pPath)
    {
        File file1 = this.assetIndex.getRootFile(pPath);

        if (file1 != null && file1.exists())
        {
            try
            {
                return new FileInputStream(file1);
            }
            catch (FileNotFoundException filenotfoundexception)
            {
            }
        }

        return super.getResourceAsStream(pPath);
    }

    public Collection<ResourceLocation> getResources(PackType pType, String pNamespace, String pPath, int pMaxDepth, Predicate<String> pFilter)
    {
        Collection<ResourceLocation> collection = super.getResources(pType, pNamespace, pPath, pMaxDepth, pFilter);
        collection.addAll(this.assetIndex.getFiles(pPath, pNamespace, pMaxDepth, pFilter));
        return collection;
    }
}
