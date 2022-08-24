package com.workerai.client.modules.automine;

import com.workerai.client.WorkerClient;
import com.workerai.client.modules.AbstractModule;
import com.workerai.client.modules.automine.config.AutomineConfig;
import com.workerai.client.modules.automine.screen.AutomineScreen;
import com.workerai.client.modules.utils.AbstractModuleConfig;
import net.minecraft.client.gui.screens.Screen;

public class AutomineModule extends AbstractModule<AutomineConfig> {
    private AutomineConfig automineConfig;

    public AutomineModule() {
        this.automineConfig = new AutomineConfig();
    }

    @Override
    public boolean hasModuleAccess() {
        return WorkerClient.getInstance().getTokenResponse().hasAutomine();
    }

    @Override
    public String getModuleName() {
        return "Automine";
    }

    @Override
    public Screen getModuleScreen() {
        return new AutomineScreen(this, (AutomineConfig) WorkerClient.getInstance().getModuleConfig().getConfig(this));
    }

    @Override
    public AutomineConfig getModuleConfig() {
        return this.automineConfig;
    }

    @Override
    public String getModuleDescription() {
        return "Automatically mine cobblestone.";
    }

    @Override
    public Class<? extends AbstractModuleConfig> getModuleConfigClass() {
        return AutomineConfig.class;
    }

    @Override
    public void onModuleConfigChange(AutomineConfig newModuleConfig) {
        if (this.automineConfig.getKeybind() != newModuleConfig.getKeybind()) {
            this.automineConfig.setKeybind(newModuleConfig.getKeybind());
        }
        this.automineConfig = newModuleConfig;
    }
}
