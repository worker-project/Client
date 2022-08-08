package com.workerai.event.interact;

import com.workerai.event.Event;

public class KeyPressedEvent extends Event {
    private final int key;
    private final boolean repeat;

    public KeyPressedEvent(int key, boolean isRepeat) {
        this.key = key;
        repeat = isRepeat;
    }

    public int getKey() {
        return key;
    }

    public boolean isRepeat() {
        return repeat;
    }
}
