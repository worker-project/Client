package com.workerai.client.handlers.hypixel;

import com.workerai.client.utils.ReconnectUtils;
import com.workerai.event.network.server.ReconnectEvent;
import com.workerai.event.utils.InvokeEvent;

public class ServerReconnect {
    @InvokeEvent()
    public void onHypixelServerChange(ReconnectEvent event) {
        ReconnectUtils.setCurrentServerID(event.getCurrentServerID());
        ReconnectUtils.connectToServer();
    }
}
