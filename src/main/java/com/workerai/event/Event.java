package com.workerai.event;

public class Event {
    public void post() {
        EventBus.INSTANCE.post(this);
    }
}
