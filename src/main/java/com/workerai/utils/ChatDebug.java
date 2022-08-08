package com.workerai.utils;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

public class ChatDebug {
    public static void sendGuiMessage(String... messages) {
        for (String message : messages) {
            Minecraft.getInstance().player.sendMessage(new TextComponent("\u00A7" + message), Util.NIL_UUID);
        }
    }
}
