package com.workerai.event.network.server;

import com.workerai.event.Event;

public class ReconnectEvent extends Event {
    private final int currentServerID;
    private final String currentServerName;

    public ReconnectEvent(int currentServerID, String currentServerName) {
        this.currentServerID = currentServerID;
        this.currentServerName = currentServerName;
    }

    public int getCurrentServerID() {
        return currentServerID;
    }

    public String getCurrentServerName() {
        return currentServerName;
    }
}
