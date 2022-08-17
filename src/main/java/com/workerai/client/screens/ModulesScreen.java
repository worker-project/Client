package com.workerai.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.WorkerClient;
import com.workerai.client.handlers.hypixel.ServerDetector;
import com.workerai.client.modules.AbstractModule;
import com.workerai.client.modules.utils.ModuleScreenManager;
import com.workerai.client.screens.components.GuiButtonBack;
import com.workerai.client.screens.components.GuiButtonSettings;
import com.workerai.client.screens.components.GuiButtonToggle;
import com.workerai.client.utils.ReconnectUtils;
import com.workerai.utils.ResourceManager;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ModulesScreen extends Screen {
    private final Screen lastScreen;

    public ModulesScreen(Screen pLastScreen) {
        super(new TranslatableComponent("WorkerClient - Settings"));
        this.lastScreen = pLastScreen;
    }

    @Override
    public void init() {
        this.addRenderableWidget(new GuiButtonBack((this.width / 2) + 105, (this.height / 2) - 74, 12, 12, new TextComponent(""), ResourceManager.CLOSE_ICON, (p_96274_) ->
        {
            this.minecraft.setScreen(new PauseScreen(true));
        }));

        int moduleID = 0;
        for (AbstractModule module : WorkerClient.getInstance().getHandlersManager().getWorkerScripts().getModules()) {
            this.addRenderableWidget(new GuiButtonSettings((this.width / 2) - 105 + (moduleID * 65) + 5, (this.height / 2) - 39, 50, 14, new TextComponent("Config"), new TextComponent("Settings"), (p_96274_) ->
            {
                this.minecraft.setScreen(module.getModuleScreen());
            }));
            this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 105 + (moduleID * 65) + 15, (this.height / 2) - 21, 30, 12, new TextComponent(""), new TextComponent(module.getModuleDescription()), module.getModuleConfig().isModuleEnabled(), (p_96274_) ->
            {
                handleModuleAction(module);
            }));
            moduleID++;
        }
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        ModuleScreenManager.drawBackground(pPoseStack, this, this.title);
        ModuleScreenManager.drawModules(pPoseStack, this, WorkerClient.getInstance().getHandlersManager().getWorkerScripts().getModules());
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    void handleModuleAction(AbstractModule module) {
        if(ServerDetector.getInstance().isInHypixel() && module.hasModuleAccess()) {
            // turning off other modules except module turned on/off
            module.setOtherModulesEnabled(false);

            // turning module on/off
            module.getModuleConfig().setModuleEnabled(!module.getModuleConfig().isModuleEnabled(), true);
            module.setModuleConfig(module.getModuleConfig());

            boolean moduleEnabled = module.getModuleConfig().isModuleEnabled();

            // do some more actions required when module turned on
            switch (module.getModuleName()) {
                case "Automine":
                    if(moduleEnabled) ReconnectUtils.connectToServer();
                case "Fairy":
                    if(moduleEnabled) System.out.println("Fairy module debug soutv");
                default:
                    if(moduleEnabled) System.out.println("Default module debug soutv");
            }

            this.minecraft.setScreen(new ModulesScreen(this));
        }
    }
}