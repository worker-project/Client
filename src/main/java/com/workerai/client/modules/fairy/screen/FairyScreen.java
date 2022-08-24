package com.workerai.client.modules.fairy.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.modules.fairy.FairyModule;
import com.workerai.client.modules.fairy.config.FairyConfig;
import com.workerai.client.screens.ModuleScreenManager;
import com.workerai.client.screens.ModulesScreen;
import com.workerai.client.screens.components.GuiButtonBack;
import com.workerai.client.screens.components.GuiButtonKey;
import com.workerai.client.screens.components.GuiButtonToggle;
import com.workerai.utils.ResourceManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class FairyScreen extends Screen {
    private final FairyModule module;

    public FairyScreen(FairyModule module, FairyConfig config) {
        super(new TextComponent("Settings | " + module.getModuleName() + " Module"));
        this.module = module;

        this.module.setModuleConfig(config);
    }

    @Override
    public void init() {
        this.addRenderableWidget(new GuiButtonBack((this.width / 2) + 130, (this.height / 2) - 74, 12, 12, new TextComponent(""), ResourceManager.BACK_ICON, (p_96274_) ->
        {
            this.minecraft.setScreen(new ModulesScreen(this));
        }));

        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120, (this.height / 2) - 37, 50, 16, new TextComponent("Hub"), new TextComponent("Display missing fairies in HUB."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.HUB), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.HUB), FairyConfig.LOCATION.HUB, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120 + 15 + 50, (this.height / 2) - 37, 50, 16, new TextComponent("Winter"), new TextComponent("Display missing fairies in WINTER."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.WINTER), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.WINTER), FairyConfig.LOCATION.WINTER, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120 + 15 * 2 + 50 * 2, (this.height / 2) - 37, 50, 16, new TextComponent("Dungeon"), new TextComponent("Display missing fairies in DUNGEON."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.DUNGEON), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.DUNGEON), FairyConfig.LOCATION.DUNGEON, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120 + 15 * 3 + 50 * 3, (this.height / 2) - 37, 50, 16, new TextComponent("Foraging"), new TextComponent("Display missing fairies in FORAGING."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.FORAGING), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.FORAGING), FairyConfig.LOCATION.FORAGING, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120, (this.height / 2) + 3, 50, 16, new TextComponent("Crimson"), new TextComponent("Display missing fairies in CRIMSON."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.CRIMSON), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.CRIMSON), FairyConfig.LOCATION.CRIMSON, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120 + 15 + 50, (this.height / 2) + 3, 50, 16, new TextComponent("Farming"), new TextComponent("Display missing fairies in FARMING."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.FARMING), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.FARMING), FairyConfig.LOCATION.FARMING, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120 + 15 * 2 + 50 * 2, (this.height / 2) + 3, 50, 16, new TextComponent("Combat 1"), new TextComponent("Display missing fairies in COMBAT 1."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.COMBAT_1), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.COMBAT_1), FairyConfig.LOCATION.COMBAT_1, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120 + 15 * 3 + 50 * 3, (this.height / 2) + 3, 50, 16, new TextComponent("Combat 3"), new TextComponent("Display missing fairies in COMBAT 3."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.COMBAT_3), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.COMBAT_3), FairyConfig.LOCATION.COMBAT_3, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120, (this.height / 2) + 43, 50, 16, new TextComponent("Gold Mine"), new TextComponent("Display missing fairies in GOLD MINE."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.GOLD_MINE), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.GOLD_MINE), FairyConfig.LOCATION.GOLD_MINE, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120 + 15 + 50, (this.height / 2) + 43, 50, 16, new TextComponent("Caverns"), new TextComponent("Display missing fairies in DEEP CAVERNS."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.DEEP_CAVERNS), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.DEEP_CAVERNS), FairyConfig.LOCATION.DEEP_CAVERNS, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((this.width / 2) - 120 + 15 * 2 + 50 * 2, (this.height / 2) + 43, 50, 16, new TextComponent("Mining 3"), new TextComponent("Display missing fairies in MINING 3."), this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.MINING_3), (p_96274_) ->
        {
            this.module.getModuleConfig().setDisplay(!this.module.getModuleConfig().isDisplay(FairyConfig.LOCATION.MINING_3), FairyConfig.LOCATION.MINING_3, true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));

        this.addRenderableWidget(new GuiButtonKey((this.width / 2) - 120 + 15 * 3 + 50 * 3, (height / 2) + 43, 50, 16, new TextComponent("KeyBind"), new TextComponent("Keyboard key used to turn fairy On/Off."), this.module, (p_96274_) ->
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
