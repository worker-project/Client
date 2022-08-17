package com.workerai.client.screens.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.modules.AbstractModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.workerai.client.modules.utils.ModuleScreenManager.*;

public class GuiButtonSettings extends Button {
    public AbstractModule module;

    public final Component description;
    public float hoveredTime;

    public GuiButtonSettings(int pX, int pY, int pWidth, int pHeight, Component pTitle, Component pDescription, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pTitle, pOnPress);

        this.description = pDescription;
    }

    public GuiButtonSettings setModule(AbstractModule module) {
        this.module = module;
        return this;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF222831);

        drawCenteredString(pPoseStack, Minecraft.getInstance().font,this.getMessage(), this.x + (this.width/2), this.y + (this.height/2) - 4, CUSTOM_WHITE);

        if(this.isHovered) {
            fill(pPoseStack, this.x +1, this.y, this.x, this.y + this.height, CUSTOM_YELLOW);
            fill(pPoseStack, this.x + this.width -1, this.y, this.x + this.width, this.y + this.height, CUSTOM_YELLOW);
        }
    }



    public Component getDescription() {
        return description;
    }
}
