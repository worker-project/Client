package com.workerai.client.screens.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.workerai.client.modules.utils.ModuleScreenManager.*;

public class GuiButtonToggle extends Button {
    private final Component description;
    private float hoveredTime;
    private boolean enabled;
    private PoseStack poseStack;

    public GuiButtonToggle(int pX, int pY, int pWidth, int pHeight, Component pTitle, Component pDescription, boolean enabled, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pTitle, pOnPress);

        this.enabled = enabled;
        this.description = pDescription;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.poseStack = pPoseStack;

        if (hasText()) {
            fill(pPoseStack, this.x - 5, this.y - 15, this.x + this.width + 5, this.y + this.height + 4, CUSTOM_LIGHT_GRAY);
        }
        fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, CUSTOM_DARK_GRAY);

        if (this.isHovered) {
            fill(pPoseStack, this.x + 1, this.y, this.x, this.y + this.height, CUSTOM_YELLOW);
            fill(pPoseStack, this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, CUSTOM_YELLOW);
        }

        drawCenteredString(pPoseStack, Minecraft.getInstance().font, this.getMessage(), this.x + (this.width / 2), this.y - 12, CUSTOM_WHITE);
        drawCenteredString(pPoseStack, Minecraft.getInstance().font, enabled ? "On" : "Off", this.x + (this.width / 2), this.y + (this.height / 2) - 4, enabled ? CUSTOM_GREEN : CUSTOM_RED);
    }

    @Override
    public void onClick(double pMouseX, double p_93372_) {
        this.setEnabled(!enabled);
        drawCenteredString(poseStack, Minecraft.getInstance().font, isEnabled() ? "On" : "Off", this.x + (this.width / 2), this.y + (this.height / 2) - 4, isEnabled() ? CUSTOM_GREEN : CUSTOM_RED);
        super.onClick(pMouseX, p_93372_);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasText() {
        return !this.getMessage().getString().equals("");
    }

    public Component getDescription() {
        return description;
    }
}
