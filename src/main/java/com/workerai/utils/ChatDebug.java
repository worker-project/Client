package com.workerai.utils;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

public class ChatDebug {
    public static void sendGuiMessage(String message) {
        Minecraft.getInstance().player.sendMessage(new TextComponent(message), Util.NIL_UUID);
    }
}
