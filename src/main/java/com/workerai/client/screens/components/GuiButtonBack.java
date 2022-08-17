package com.workerai.client.screens.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL33;

public class GuiButtonBack extends Button {
    private final ResourceLocation resource;

    public GuiButtonBack(int pX, int pY, int pWidth, int pHeight, Component pMessage, ResourceLocation resource, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
        this.resource = resource;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF222831);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GL33.glEnable(GL33.GL_MULTISAMPLE);
        if (this.isHovered) {
            blit(pPoseStack, this.x + 1, this.y + 1, 12 - 2, 12 - 2, 0, 0, 512, 512, 512, 512);
        } else {
            blit(pPoseStack, this.x + 2, this.y + 2, 12 - 4, 12 - 4, 0, 0, 512, 512, 512, 512);
        }
        GL33.glDisable(GL33.GL_MULTISAMPLE);
    }
}
