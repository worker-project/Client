package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterTropicalFishPatternB extends ModelAdapterTropicalFishB
{
    public ModelAdapterTropicalFishPatternB()
    {
        super(EntityType.TROPICAL_FISH, "tropical_fish_pattern_b", 0.2F);
    }

    public Model makeModel()
    {
        return new TropicalFishModelB(bakeModelLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer entityrenderer = entityrenderdispatcher.getEntityRenderMap().get(EntityType.TROPICAL_FISH);

        if (!(entityrenderer instanceof TropicalFishRenderer))
        {
            Config.warn("Not a RenderTropicalFish: " + entityrenderer);
            return null;
        }
        else
        {
            if (entityrenderer.getType() == null)
            {
                TropicalFishRenderer tropicalfishrenderer = new TropicalFishRenderer(entityrenderdispatcher.getContext());
                tropicalfishrenderer.model = new TropicalFishModelB<>(bakeModelLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
                tropicalfishrenderer.shadowRadius = 0.2F;
                entityrenderer = tropicalfishrenderer;
            }

            TropicalFishRenderer tropicalfishrenderer1 = (TropicalFishRenderer)entityrenderer;
            TropicalFishPatternLayer tropicalfishpatternlayer = (TropicalFishPatternLayer)tropicalfishrenderer1.getLayer(TropicalFishPatternLayer.class);

            if (tropicalfishpatternlayer == null || !tropicalfishpatternlayer.custom)
            {
                tropicalfishpatternlayer = new TropicalFishPatternLayer(tropicalfishrenderer1, entityrenderdispatcher.getContext().getModelSet());
                tropicalfishpatternlayer.custom = true;
            }

            if (!Reflector.TropicalFishPatternLayer_modelB.exists())
            {
                Config.warn("Field not found: TropicalFishPatternLayer.modelB");
                return null;
            }
            else
            {
                Reflector.TropicalFishPatternLayer_modelB.setValue(tropicalfishpatternlayer, modelBase);
                tropicalfishrenderer1.removeLayers(TropicalFishPatternLayer.class);
                tropicalfishrenderer1.addLayer(tropicalfishpatternlayer);
                return tropicalfishrenderer1;
            }
        }
    }

    public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation)
    {
        TropicalFishRenderer tropicalfishrenderer = (TropicalFishRenderer)er;

        for (TropicalFishPatternLayer tropicalfishpatternlayer : tropicalfishrenderer.getLayers(TropicalFishPatternLayer.class))
        {
            TropicalFishModelB tropicalfishmodelb = (TropicalFishModelB)Reflector.TropicalFishPatternLayer_modelB.getValue(tropicalfishpatternlayer);

            if (tropicalfishmodelb != null)
            {
                tropicalfishmodelb.locationTextureCustom = textureLocation;
            }
        }

        return true;
    }
}
