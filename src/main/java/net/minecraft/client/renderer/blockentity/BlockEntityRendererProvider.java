package net.minecraft.client.renderer.blockentity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface BlockEntityRendererProvider<T extends BlockEntity>
{
    BlockEntityRenderer<T> create(BlockEntityRendererProvider.Context pContext);

    public static class Context
    {
        private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
        private final BlockRenderDispatcher blockRenderDispatcher;
        private final EntityModelSet modelSet;
        private final Font font;

        public Context(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, BlockRenderDispatcher pBlockRenderDispatcher, EntityModelSet pModelSet, Font pFont)
        {
            this.blockEntityRenderDispatcher = pBlockEntityRenderDispatcher;
            this.blockRenderDispatcher = pBlockRenderDispatcher;
            this.modelSet = pModelSet;
            this.font = pFont;
        }

        public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher()
        {
            return this.blockEntityRenderDispatcher;
        }

        public BlockRenderDispatcher getBlockRenderDispatcher()
        {
            return this.blockRenderDispatcher;
        }

        public EntityModelSet getModelSet()
        {
            return this.modelSet;
        }

        public ModelPart bakeLayer(ModelLayerLocation pLayerLocation)
        {
            return this.modelSet.bakeLayer(pLayerLocation);
        }

        public Font getFont()
        {
            return this.font;
        }
    }
}
