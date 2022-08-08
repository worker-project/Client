package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemModelShaper
{
    public final Int2ObjectMap<ModelResourceLocation> shapes = new Int2ObjectOpenHashMap<>(256);
    private final Int2ObjectMap<BakedModel> shapesCache = new Int2ObjectOpenHashMap<>(256);
    private final ModelManager modelManager;

    public ItemModelShaper(ModelManager pModelManager)
    {
        this.modelManager = pModelManager;
    }

    public BakedModel getItemModel(ItemStack pItem)
    {
        BakedModel bakedmodel = this.getItemModel(pItem.getItem());
        return bakedmodel == null ? this.modelManager.getMissingModel() : bakedmodel;
    }

    @Nullable
    public BakedModel getItemModel(Item pItem)
    {
        return this.shapesCache.get(getIndex(pItem));
    }

    private static int getIndex(Item pItem)
    {
        return Item.getId(pItem);
    }

    public void register(Item pItem, ModelResourceLocation pModelLocation)
    {
        this.shapes.put(getIndex(pItem), pModelLocation);
    }

    public ModelManager getModelManager()
    {
        return this.modelManager;
    }

    public void rebuildCache()
    {
        this.shapesCache.clear();

        for (Entry<Integer, ModelResourceLocation> entry : this.shapes.entrySet())
        {
            this.shapesCache.put(entry.getKey(), this.modelManager.getModel(entry.getValue()));
        }
    }
}
