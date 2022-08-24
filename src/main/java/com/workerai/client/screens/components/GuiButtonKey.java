package com.workerai.client.screens.components;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.modules.AbstractModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import static com.workerai.client.screens.ModuleScreenManager.*;

public class GuiButtonKey extends Button {
    private final AbstractModule module;
    private PoseStack poseStack;
    private int keyCode;
    private String displayedCode;
    public float hoveredTime;
    private final Component description;

    private static boolean isEditingKey = false;

    public GuiButtonKey(int pX, int pY, int pWidth, int pHeight, Component pTitle, Component pDescription, AbstractModule module, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pTitle, pOnPress);

        this.module = module;
        this.keyCode = module.getModuleConfig().getKeybind();
        this.displayedCode = keyCode == 999 ? "None" : GLFW.glfwGetKeyName(keyCode, -1);
        this.description = pDescription;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.poseStack = pPoseStack;

        fill(pPoseStack, this.x - 5, this.y - 15, this.x + this.width + 5, this.y + this.height + 4, CUSTOM_LIGHT_GRAY);
        fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, CUSTOM_DARK_GRAY);

        if (this.isHovered) {
            fill(pPoseStack, this.x + 1, this.y, this.x, this.y + this.height, CUSTOM_YELLOW);
            fill(pPoseStack, this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, CUSTOM_YELLOW);
        }

        drawCenteredString(pPoseStack, Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y - 12, CUSTOM_WHITE);
        drawCenteredString(pPoseStack, Minecraft.getInstance().font, displayedCode.toUpperCase(), this.x + (width / 2), this.y + 4, displayedCode.equals("None") || displayedCode.equals("...") ? CUSTOM_RED : CUSTOM_GREEN);
    }

    @Override
    public void onPress() {
        isEditingKey = true;
        this.displayedCode = "...";
        this.keyCode = 999;
        this.module.getModuleConfig().setKeybind(this.keyCode);
        super.onPress();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (!isEditingKey) return false;

        this.keyCode = pKeyCode;
        if (this.keyCode == InputConstants.KEY_ESCAPE) {
            this.keyCode = 999;
            this.displayedCode = "None";
        } else {
            this.displayedCode = InputConstants.getKey(pKeyCode, pScanCode).getDisplayName().getString();
        }

        this.module.getModuleConfig().setKeybind(this.keyCode);
        this.module.setModuleConfig(this.module.getModuleConfig());
        drawCenteredString(this.poseStack, Minecraft.getInstance().font, displayedCode.toUpperCase(), this.x + (width / 2), this.y + 4, CUSTOM_GREEN);
        isEditingKey = false;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public Component getDescription() {
        return description;
    }

    public static boolean isIsEditingKey() {
        return isEditingKey;
    }
}
