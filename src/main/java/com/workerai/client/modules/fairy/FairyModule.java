package com.workerai.client.modules.fairy;

import com.workerai.client.WorkerClient;
import com.workerai.client.modules.AbstractModule;
import com.workerai.client.modules.fairy.config.FairyConfig;
import com.workerai.client.modules.fairy.screen.FairyScreen;
import com.workerai.client.modules.utils.AbstractModuleConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.block.Block;

public class FairyModule extends AbstractModule<FairyConfig> {
    private FairyConfig fairyConfig;

    public FairyModule() {
        fairyConfig = new FairyConfig();
    }

    @Override
    public boolean hasModuleAccess() {
        return WorkerClient.getInstance().getTokenResponse().hasAutomine();
    }

    @Override
    public String getModuleName() {
        return "Fairy";
    }

    @Override
    public FairyConfig getModuleConfig() {
        return fairyConfig;
    }

    @Override
    public Screen getModuleScreen() {
        return new FairyScreen(this);
    }

    @Override
    public String getModuleDescription() {
        return "Highlights all fairy souls.";
    }

    @Override
    public Class<? extends AbstractModuleConfig> getModuleConfigClass() {
        return FairyConfig.class;
    }

    @Override
    public void onModuleConfigChange(FairyConfig newModuleConfig) {
        if (fairyConfig.getKeybind() != newModuleConfig.getKeybind()) {
            fairyConfig.setKeybind(newModuleConfig.getKeybind());
        }
        fairyConfig = newModuleConfig;
    }
}