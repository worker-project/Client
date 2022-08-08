package com.workerai.event.network.server;

import com.workerai.event.Event;

public class ServerJoinEvent extends Event {

    private final String server;
    private final int port;

    public ServerJoinEvent(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }
}
