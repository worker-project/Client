package com.workerai.client.handlers.hypixel;

import com.workerai.event.network.server.ReconnectEvent;
import com.workerai.event.utils.InvokeEvent;
import com.workerai.client.utils.ReconnectUtils;

public class ServerReconnect {
    @InvokeEvent()
    public void onHypixelServerChange(ReconnectEvent event) {
        ReconnectUtils.setCurrentServerID(event.getCurrentServerID());
        ReconnectUtils.connectToServer();
    }
}
