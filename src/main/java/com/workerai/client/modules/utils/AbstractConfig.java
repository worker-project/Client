package com.workerai.client.modules.utils;

import com.workerai.utils.ChatUtils;

public abstract class AbstractConfig {
    public abstract int getKeybind();

    public abstract void setKeybind(int keyBind);

    public abstract boolean isModuleEnabled();

    public abstract void setModuleEnabled(boolean active, boolean debug);

    protected void displayDebugMessage(String moduleName, boolean active, String setting) {
        ChatUtils.sendGuiMessage(
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
