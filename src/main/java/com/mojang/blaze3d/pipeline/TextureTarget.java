package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;

public class TextureTarget extends RenderTarget
{
    public TextureTarget(int pWidth, int pHeight, boolean pUseDepth, boolean pClearError)
    {
        super(pUseDepth);
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(pWidth, pHeight, pClearError);
    }
}
