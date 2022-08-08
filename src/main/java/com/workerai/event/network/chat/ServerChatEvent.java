package com.workerai.event.network.chat;

import com.workerai.event.Event;
import net.minecraft.network.chat.Component;

public class ServerChatEvent extends Event {
    private final Component chat;
    private final boolean focus;

    public ServerChatEvent(Component chat, boolean focus) {
        this.chat = chat;
        this.focus = focus;
    }

    public Component getChat() {
        return chat;
    }

    public boolean isFocus() {
        return focus;
    }
}
