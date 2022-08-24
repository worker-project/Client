package com.workerai.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.WorkerClient;
import com.workerai.client.handlers.hypixel.ServerDetector;
import com.workerai.client.modules.AbstractModule;
import com.workerai.client.screens.components.GuiButtonBack;
import com.workerai.client.screens.components.GuiButtonSettings;
import com.workerai.client.screens.components.GuiButtonToggle;
import com.workerai.client.utils.ReconnectUtils;
import com.workerai.utils.AccessUtils;
import com.workerai.utils.ResourceManager;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class ModulesScreen extends Screen {
    private final Screen lastScreen;

    public ModulesScreen(Screen pLastScreen) {
        super(new TextComponent("WorkerClient - Settings"));
        this.lastScreen = pLastScreen;
    }

    @Override
    public void init() {
        this.addRenderableWidget(new GuiButtonBack((this.width / 2) + 130, (this.height / 2) - 74, 12, 12, new TextComponent(""), ResourceManager.CLOSE_ICON, (p_96274_) ->
        {
            this.minecraft.setScreen(new PauseScreen(true));
        }));

        int moduleID = 0;
        for (AbstractModule module : AccessUtils.getInstance().getModules()) {
            this.addRenderableWidget(new GuiButtonSettings((this.width / 2) - 120 + 15*moduleID + 50*moduleID, (this.height / 2) - 37, 50, 16, new TextComponent("Settings"), new TextComponent(module.getModuleDescription()), (p_96274_) ->
            {
                this.minecraft.setScreen(module.getModuleScreen());
            }));
            /*this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 125 + (moduleID * 65) + 15, (this.height / 2) - 21, 30, 12, new TextComponent(""), new TextComponent(module.getModuleDescription()), module.getModuleConfig().isModuleEnabled(), (p_96274_) ->
            {
                moduleGlobalEnable(module);
            }));*/
            moduleID++;
        }
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        ModuleScreenManager.drawBackground(pPoseStack, this, this.title);
        ModuleScreenManager.drawModules(pPoseStack, this, AccessUtils.getInstance().getModules());
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    void moduleGlobalEnable(AbstractModule module) {
        if(ServerDetector.getInstance().isInHypixel() && module.hasModuleAccess()) {
            module.setOtherModulesDisabled(true);

            module.getModuleConfig().setModuleEnabled(!module.getModuleConfig().isModuleEnabled(), true);
            module.setModuleConfig(module.getModuleConfig());

            boolean moduleEnabled = module.getModuleConfig().isModuleEnabled();

            switch (module.getModuleName()) {
                case "Automine":
                    if(moduleEnabled) ReconnectUtils.connectToServer();
                    break;
                case "Fairy":
                    if(moduleEnabled) System.out.println("Fairy module debug soutv");
                    break;
                default:
                    if(moduleEnabled) System.out.println("Default module debug soutv");
                    break;
            }

            this.minecraft.setScreen(new ModulesScreen(this));
        }
    }
}