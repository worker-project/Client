package com.workerai.client.modules.automine.config;

import com.workerai.client.modules.utils.AbstractModuleConfig;

public class AutomineConfig extends AbstractModuleConfig {
    private boolean automineModuleEnabled;
    private boolean autoDrop;
    private boolean autoReconnect;
    private boolean autoCraft;
    private int ghostBlockDelay;
    private int keyBind;

    public AutomineConfig(boolean autoDrop, boolean autoReconnect, boolean autoCraft, int keyBind, int ghostBlockDelay) {
        this.automineModuleEnabled = false;

        this.autoDrop = autoDrop;
        this.autoReconnect = autoReconnect;
        this.autoCraft = autoCraft;
        this.ghostBlockDelay = ghostBlockDelay;

        this.keyBind = keyBind;
    }

    public AutomineConfig() {
        AutomineConfig config = getDefaultConfig();

        this.automineModuleEnabled = false;

        this.autoDrop = config.isAutoDrop();
        this.autoReconnect = config.isAutoReconnect();
        this.autoCraft = config.isAutoCraft();
        this.ghostBlockDelay = config.getGhostBlockDelay();

        this.keyBind = config.keyBind;
    }

    @Override
    public int getKeybind() {
        return keyBind;
    }

    @Override
    public boolean isModuleEnabled() {
        return automineModuleEnabled;
    }

    @Override
    public void setKeybind(int keyBind) {
        this.keyBind = keyBind;
    }

    @Override
    public void setModuleEnabled(boolean active, boolean debug) {
        this.automineModuleEnabled = active;
        if (debug) displayDebugMessage("Module", active, "Automine");
    }

    @Override
    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    @Override
    public boolean isAutoDrop() {
        return autoDrop;
    }

    @Override
    public boolean isAutoCraft() {
        return autoCraft;
    }

    public int getGhostBlockDelay() {
        return ghostBlockDelay;
    }

    @Override
    public void setAutoReconnect(boolean active, boolean debug) {
        this.autoReconnect = active;
        if (debug) displayDebugMessage("Automine", active, "AutoReconnect");
    }

    @Override
    public void setAutoDrop(boolean active, boolean debug) {
        this.autoDrop = active;
        if (debug) displayDebugMessage("Automine", active, "AutoDrop");
    }

    @Override
    public void setAutoCraft(boolean active, boolean debug) {
        this.autoCraft = active;
        if (debug) displayDebugMessage("Automine", active, "AutoCraft");
    }

    public void setGhostBlockDelay(int delay) {
        this.ghostBlockDelay = delay;
    }

    @Override
    public AutomineConfig getDefaultConfig() {
        return new AutomineConfig(false, false, false, 999, 10);
    }
}
