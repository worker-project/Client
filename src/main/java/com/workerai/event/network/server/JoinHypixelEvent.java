package com.workerai.event.network.server;

import com.google.common.base.Preconditions;
import com.workerai.event.Event;
import org.jetbrains.annotations.NotNull;

public class JoinHypixelEvent extends Event {
    @NotNull
    private final ServerVerificationMethod method;

    public JoinHypixelEvent(@NotNull ServerVerificationMethod method) {
        Preconditions.checkNotNull(method, "method");
        this.method = method;
    }

    @NotNull
    public ServerVerificationMethod getMethod() {
        return method;
    }

    public enum ServerVerificationMethod {
        IP
    }
}
