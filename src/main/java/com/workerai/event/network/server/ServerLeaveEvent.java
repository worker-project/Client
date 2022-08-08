package com.workerai.event.network.server;

import com.workerai.event.Event;

public class ServerLeaveEvent extends Event {

    private final String server;
    private final int port;

    public ServerLeaveEvent() {
        this.server = "";
        this.port = -1;
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }
}
