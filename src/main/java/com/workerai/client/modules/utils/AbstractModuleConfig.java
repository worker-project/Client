package com.workerai.client.modules.utils;

public abstract class AbstractModuleConfig extends AbstractConfig {
    public boolean isAutoReconnect() {
        return false;
    }

    public boolean isAutoDrop() {
        return false;
    }

    public boolean isAutoCraft() {
        return false;
    }

    public void setAutoReconnect(boolean active, boolean debug) {
    }

    public void setAutoDrop(boolean active, boolean debug) {
    }

    public void setAutoCraft(boolean active, boolean debug) {
    }

    public abstract AbstractModuleConfig getDefaultConfig();
}
