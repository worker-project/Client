package com.workerai.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.modules.AbstractModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class ModuleScreenManager {
    public static final int CUSTOM_LIGHT_GRAY = 0xFF393E46;
    public static final int CUSTOM_LIGHT_BLUE = 0xFF7878FF;
    public static final int CUSTOM_DARK_GRAY = 0xFF222831;
    public static final int CUSTOM_WHITE = 0xFFFFFF;
    public static final int CUSTOM_YELLOW = 0xFFF0A500;
    public static final int CUSTOM_GREEN = 0xFF21bf73;
    public static final int CUSTOM_RED = 0xFFcf1b1b;

    public static void drawBackground(PoseStack pPoseStack, Screen pScreen, Component pTitle) {
        GuiComponent.fill(pPoseStack, (pScreen.width / 2) - 141, (pScreen.height / 2) - 78, (pScreen.width / 2) + 146, (pScreen.height / 2) + 78, CUSTOM_YELLOW);
        GuiComponent.fill(pPoseStack, (pScreen.width / 2) - 140, (pScreen.height / 2) - 77, (pScreen.width / 2) + 145, (pScreen.height / 2) + 77, CUSTOM_LIGHT_GRAY);
        GuiComponent.fill(pPoseStack, (pScreen.width / 2) - 130, (pScreen.height / 2) - 57, (pScreen.width / 2) + 135, (pScreen.height / 2) + 67, CUSTOM_DARK_GRAY);

        GuiComponent.drawCenteredString(pPoseStack, Minecraft.getInstance().font, pTitle, pScreen.width / 2, (pScreen.height / 2) - 70, CUSTOM_YELLOW);
    }

    public static void drawModules(PoseStack pPoseStack, Screen pScreen, List<AbstractModule> pModules) {
        int moduleID = 0;
        for (AbstractModule module : pModules) {
            GuiComponent.fill(pPoseStack, ((pScreen.width / 2) - 105 + (moduleID * 65)), (pScreen.height / 2) - 52, ((pScreen.width / 2) - 105 + (moduleID * 65)) + 60, (pScreen.height / 2) - 7, CUSTOM_LIGHT_GRAY);
            GuiComponent.drawCenteredString(pPoseStack, Minecraft.getInstance().font, new TextComponent(module.getModuleName()), (pScreen.width / 2) - 105 + (moduleID * 65) + 30 - Minecraft.getInstance().font.width(module.getModuleName()), (pScreen.height / 2) - 50, CUSTOM_YELLOW);
            moduleID++;
        }
    }
}
