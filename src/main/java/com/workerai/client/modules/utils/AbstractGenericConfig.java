package com.workerai.client.modules.utils;

public abstract class AbstractGenericConfig {
    public abstract int getKeybind();

    public abstract void setKeybind(int keyBind);

    public abstract boolean isModuleEnabled();

    public abstract void setModuleEnabled(boolean active, boolean debug);

    public abstract AbstractModuleConfig getDefaultConfig();
}
