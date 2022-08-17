package com.workerai.client.modules;

import com.workerai.client.WorkerClient;
import com.workerai.client.modules.utils.AbstractModuleConfig;
import net.minecraft.client.gui.screens.Screen;

public abstract class AbstractModule<T extends AbstractModuleConfig> {
    protected T moduleConfig;

    public abstract boolean hasModuleAccess();

    public abstract String getModuleName();

    public abstract T getModuleConfig();

    public abstract Screen getModuleScreen();

    public abstract String getModuleDescription();

    public abstract Class<? extends AbstractModuleConfig> getModuleConfigClass();

    public abstract void onModuleConfigChange(T newModuleConfig);

    public void setModuleConfig(T config) {
        this.moduleConfig = config;
        WorkerClient.getInstance().getModuleConfig().setConfig(this, config);
    }

    public void setOtherModulesEnabled(boolean active) {
        for(AbstractModule abstractModule : WorkerClient.getInstance().getHandlersManager().getWorkerScripts().getModules()) {
            if(abstractModule != this && abstractModule.getModuleConfig().isModuleEnabled()) {
                abstractModule.getModuleConfig().setModuleEnabled(active, true);
                abstractModule.setModuleConfig(abstractModule.getModuleConfig());
            }
        }
    }
}
