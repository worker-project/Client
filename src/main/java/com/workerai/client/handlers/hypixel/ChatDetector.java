package com.workerai.client.handlers.hypixel;

import com.workerai.client.modules.fairy.FairyModule;
import com.workerai.event.network.chat.ServerChatEvent;
import com.workerai.event.utils.InvokeEvent;
import com.workerai.utils.AccessUtils;

import java.util.Objects;

public class ChatDetector {
    @InvokeEvent
    public void onServerChatEvent(ServerChatEvent event) {
        if (ServerDetector.getInstance().isInHypixel()) {
            if (event.getChat().getString().contains("You are AFK. Move around to return from AFK.")) {
                AccessUtils.getInstance().getScoreboardDetector().refreshCurrentServer(true);
            }


            if (event.getChat().getString().contains("You have already found that Fairy Soul!") || event.getChat().getString().contains("SOUL! You found a Fairy Soul!")) {
                if (Objects.requireNonNull(event.getChat().getSiblings().get(0).getStyle().getColor()).toString().equals("light_purple")) {
                    if (AccessUtils.getInstance().getSurroundingFairies().getPrevBlockToCheck() != null) {
                        FairyModule module = AccessUtils.getInstance().getModuleHandler().getModule("Fairy");
                        module.getModuleConfig().addCollectedFairies(AccessUtils.getInstance().getSurroundingFairies().getPrevBlockToCheck());
                        module.setModuleConfig(module.getModuleConfig());
                    }
                }
            }
        }
    }
}
