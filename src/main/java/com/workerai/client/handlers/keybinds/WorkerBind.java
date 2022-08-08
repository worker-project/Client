package com.workerai.client.handlers.keybinds;

import com.mojang.logging.LogUtils;
import com.workerai.client.Handlers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

public abstract class WorkerBind {

    private final int defaultKeyCode;

    private final String description;
    private int key;

    private boolean wasPressed;

    private boolean conflicted;
    protected boolean conflictExempt;

    public WorkerBind(String description, int key) {
        defaultKeyCode = key;
        this.description = description;
        this.key = key;
    }

    public int getKeyCode() {
        return key;
    }

    public void setKeyCode(int key) {
        this.key = key;
    }

    public int getDefaultKeyCode() {
        return defaultKeyCode;
    }

    public String getKeyDescription() {
        String message = description;
        if (capitalizeDescription()) message = WordUtils.capitalizeFully(message);
        return message;
    }

    protected String getRealDescription() {
        return description;
    }

    public void setWasPressed(boolean wasPressed) {
        this.wasPressed = wasPressed;
    }

    public void setConflicted(boolean conflicted) {
        this.conflicted = conflicted;
    }

    public boolean isConflicted() {
        return conflicted;
    }

    public boolean wasPressed() {
        return wasPressed;
    }

    public boolean capitalizeDescription() {
        return true;
    }

    public void onPress() {
    }

    public void onRelease() {
    }

    public void detectConflicts() {
        conflicted = false;

        int currentKeyCode = key;

        if (currentKeyCode == 0 || conflictExempt) return;

        List<WorkerBind> otherBinds = Minecraft.getInstance().getHandlersManager().getKeyboardHandler().getWorkerKeybinds();
        otherBinds.remove(this);

        // Check for conflicts with Minecraft binds.
        for (KeyMapping keymapping : Minecraft.getInstance().options.keyMappings) {
            int code = keymapping.getDefaultKey().getValue();
            if (currentKeyCode == code) {
                conflicted = true;
                break;
            }
        }

        for (WorkerBind workerBind : otherBinds) {
            if (!workerBind.conflictExempt) {
                int keyCode = workerBind.key;
                if (currentKeyCode == keyCode) {
                    conflicted = true;
                    break;
                }
            }
        }
    }

    public abstract void reloadKey();
}
