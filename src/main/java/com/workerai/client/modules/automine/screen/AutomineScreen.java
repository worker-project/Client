package com.workerai.client.modules.automine.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.WorkerClient;
import com.workerai.client.modules.AbstractModule;
import com.workerai.client.modules.automine.AutomineModule;
import com.workerai.client.modules.automine.config.AutomineConfig;
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

public class AutomineScreen extends Screen implements GuiButtonSlider.IResponder {
    private final AutomineModule module;

    public AutomineScreen(AbstractModule module) {
        super(new TextComponent("Settings | " + module.getModuleName() + " Module"));
        this.module = (AutomineModule) module;
    }

    @Override
    public void init() {
        AutomineConfig config = (AutomineConfig) WorkerClient.getInstance().getModuleConfig().getConfig(this.module);
        this.module.setModuleConfig(config);

        this.addRenderableWidget(new GuiButtonBack((this.width / 2) + 105, (this.height / 2) - 74, 12, 12, new TextComponent(""), ResourceManager.BACK_ICON, (p_96274_) ->
        {
            this.minecraft.setScreen(new ModulesScreen(this));
        }));

        this.addRenderableWidget(new GuiButtonToggle((width / 2) - 100, (height / 2) - 37, 90, 16, new TextComponent("AutoCraft"), new TextComponent("Automatically craft enchanted cobblestone."), this.module.getModuleConfig().isAutoCraft(), (p_96274_) ->
        {
            this.module.getModuleConfig().setAutoCraft(!this.module.getModuleConfig().isAutoCraft(), true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));
        this.addRenderableWidget(new GuiButtonToggle((width / 2) + 10, (height / 2) - 37, 90, 16, new TextComponent("AutoReconnect"), new TextComponent("Automatically reconnect to private island if disconnected."), this.module.getModuleConfig().isAutoReconnect(), (p_96274_) ->
        {
            this.module.getModuleConfig().setAutoReconnect(!this.module.getModuleConfig().isAutoReconnect(), true);
            this.module.setModuleConfig(this.module.getModuleConfig());

            ReconnectUtils.connectToServer();
        }));
        this.addRenderableWidget(new GuiButtonToggle((width / 2) - 100, (height / 2) + 3, 90, 16, new TextComponent("AutoDrop"), new TextComponent("Automatically drop enchanted cobblestone."), this.module.getModuleConfig().isAutoDrop(), (p_96274_) ->
        {
            this.module.getModuleConfig().setAutoDrop(!this.module.getModuleConfig().isAutoDrop(), true);
            this.module.setModuleConfig(this.module.getModuleConfig());
        }));

        this.addRenderableWidget(new GuiButtonKey((width / 2) + 10, (height / 2) + 3, 90, 16, new TextComponent("KeyBind"), new TextComponent("Keyboard key used to turn automine On/Off."), this.module, (p_96274_) ->
        {
        }));

        this.addRenderableWidget(new GuiButtonSlider((width / 2) - 90, (height / 2) + 43, 180, 16, new TextComponent("Ghost Blocks Reconnect Delay"), new TextComponent("Prevent ghost blocks from appearing."), new IntegerOption(this.module.getModuleConfig().getGhostBlockDelay(), 1, 60, 1), this, (p_96274_) ->
        {
        }));
    }


    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        ModuleScreenManager.drawBackground(pPoseStack, this, this.title);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    /*@Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ModuleScreenManager.drawBackground(this, this.title);
        super.drawScreen(mouseX, mouseY, partialTicks);
        displayToolTip(mouseX, mouseY, partialTicks);
    }*/

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ModulesScreen(this));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !GuiButtonKey.isIsEditingKey();
    }

    @Override
    public void onChange(IntegerOption slider) {
        this.module.getModuleConfig().setGhostBlockDelay(slider.getValue());
        this.module.setModuleConfig(this.module.getModuleConfig());
    }

    /*public void displayToolTip(int mouseX, int mouseY, float partialTicks) {
        for (GuiButton button : buttonList) {
            if (button instanceof GuiButtonToggle) {
                GuiButtonToggle toggle = (GuiButtonToggle) button;
                toggle.hoveredTime = displayToolTipTimer(mouseX, mouseY, partialTicks, toggle.getHoverDescription(), toggle.hovered, toggle.hoveredTime);
            }
            if (button instanceof GuiButtonKey) {
                GuiButtonKey toggle = (GuiButtonKey) button;
                toggle.hoveredTime = displayToolTipTimer(mouseX, mouseY, partialTicks, toggle.getHoverDescription(), toggle.hovered, toggle.hoveredTime);
            }
            if (button instanceof GuiButtonSlider) {
                GuiButtonSlider toggle = (GuiButtonSlider) button;
                toggle.hoveredTime = displayToolTipTimer(mouseX, mouseY, partialTicks, toggle.getHoverDescription(), toggle.hovered, toggle.hoveredTime);
            }
        }
    }*/

    /*public float displayToolTipTimer(int mouseX, int mouseY, float partialTicks, final String hoverDescription, boolean hovered, float hoveredTime) {
        if (hovered) {
            if (Mouse.isButtonDown(0)) {
                return 0;
            }
            if (hoveredTime >= 80) {
                this.drawHoveringText(Arrays.asList(hoverDescription), mouseX, mouseY);
                return 80;
            }
            return hoveredTime += partialTicks;
        }
        return 0;
    }*/
}
