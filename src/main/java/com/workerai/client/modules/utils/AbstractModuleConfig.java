package com.workerai.client.modules.utils;

import com.workerai.utils.ChatDebug;

public abstract class AbstractModuleConfig extends AbstractGenericConfig {
    public abstract boolean isAutoReconnect();

    public abstract boolean isAutoDrop();

    public abstract boolean isAutoCraft();

    public abstract void setAutoReconnect(boolean active, boolean debug);

    public abstract void setAutoDrop(boolean active, boolean debug);

    public abstract void setAutoCraft(boolean active, boolean debug);

    protected void displayDebugMessage(String moduleName, boolean active, String setting) {
        ChatDebug.sendGuiMessage(
                String.format(
                        "§6[%s | %s] %s- Successfully %s",
                        moduleName,
                        setting,
                        active ? "§a" : "§c",
                        active ? "activated!" : "deactivated!"
                )
        );
    }
}
