package com.workerai.client.modules.fairy.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.WorkerClient;
import com.workerai.client.modules.AbstractModule;
import com.workerai.client.modules.automine.config.AutomineConfig;
import com.workerai.client.modules.fairy.FairyModule;
import com.workerai.client.modules.fairy.config.FairyConfig;
import com.workerai.client.modules.fairy.utils.FairyPositions;
import com.workerai.client.modules.utils.ModuleScreenManager;
import com.workerai.client.screens.ModulesScreen;
import com.workerai.client.screens.components.GuiButtonBack;
import com.workerai.client.screens.components.GuiButtonKey;
import com.workerai.client.screens.components.GuiButtonSlider;
import com.workerai.client.screens.components.GuiButtonToggle;
import com.workerai.client.screens.utils.IntegerOption;
import com.workerai.client.utils.ReconnectUtils;
import com.workerai.utils.ResourceManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class FairyScreen extends Screen {
    private final FairyModule module;

    public FairyScreen(AbstractModule module) {
        super(new TextComponent("Settings | " + module.getModuleName() + " Module"));
        this.module = (FairyModule) module;
    }

    @Override
    public void init() {
        FairyConfig config = (FairyConfig) WorkerClient.getInstance().getModuleConfig().getConfig(this.module);
        this.module.setModuleConfig(config);

        this.addRenderableWidget(new GuiButtonBack((this.width / 2) + 105, (this.height / 2) - 74, 12, 12, new TextComponent(""), ResourceManager.BACK_ICON, (p_96274_) ->
        {
            this.minecraft.setScreen(new ModulesScreen(this));
        }));

        this.addRenderableWidget(new GuiButtonToggle((width / 2) - 100, (height / 2) - 37, 90, 16, new TextComponent("Hub's Fairies"), new TextComponent("Display missing fairies in HUB."), this.module.getModuleConfig().isHubFairyEnabled(), (p_96274_) ->
        {
            this.module.getModuleConfig().setHubFairyEnabled(!this.module.getModuleConfig().isHubFairyEnabled(), true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((width / 2) + 10, (height / 2) - 37, 90, 16, new TextComponent("Winter's Fairies"), new TextComponent("Display missing fairies in WINTER."), this.module.getModuleConfig().isWinterFairyEnabled(), (p_96274_) ->
        {
            this.module.getModuleConfig().setWinterFairyEnabled(!this.module.getModuleConfig().isWinterFairyEnabled(), true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((width / 2) - 100, (height / 2) + 3, 90, 16, new TextComponent("Dungeon's Fairies"), new TextComponent("Display missing fairies in DUNGEON."), this.module.getModuleConfig().isAutoDrop(), (p_96274_) ->
        {
            this.module.getModuleConfig().setDungeonFairyEnabled(!this.module.getModuleConfig().isDungeonFairyEnabled(), true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        /*this.addRenderableWidget(new GuiButtonToggle((width / 2) - 100, (height / 2) + 3, 90, 16, new TextComponent("Mining 1's Fairies"), new TextComponent("Display missing fairies in MINING_1."), this.module.getModuleConfig().isAutoDrop(), (p_96274_) ->
        {

        }));
        this.addRenderableWidget(new GuiButtonToggle((width / 2) - 100, (height / 2) + 3, 90, 16, new TextComponent("Mining 2's Fairies"), new TextComponent("Display missing fairies in MINING_2."), this.module.getModuleConfig().isAutoDrop(), (p_96274_) ->
        {

        }));*/

        this.addRenderableWidget(new GuiButtonKey((width / 2) + 10, (height / 2) + 3, 90, 16, new TextComponent("KeyBind"), new TextComponent("Keyboard key used to turn fairy On/Off."), this.module, (p_96274_) ->
        {
        }));
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        ModuleScreenManager.drawBackground(pPoseStack, this, this.title);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ModulesScreen(this));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !GuiButtonKey.isIsEditingKey();
    }
}
