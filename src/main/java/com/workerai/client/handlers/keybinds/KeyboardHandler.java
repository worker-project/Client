package com.workerai.client.handlers.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.workerai.client.handlers.hypixel.ServerDetector;
import com.workerai.event.interact.KeyPressedEvent;
import com.workerai.event.utils.InvokeEvent;

import java.util.ArrayList;
import java.util.List;

public class KeyboardHandler {
    private final List<WorkerBind> workerKeybinds = new ArrayList<>();

    @InvokeEvent
    public void onKeyPressed(KeyPressedEvent e) {
        /*for (IModule module : WorkerAI.getInstance().getHandlers().getWorkerScripts().getModules()) {*/
            if (ServerDetector.getInstance().isInHypixel()/* && module.hasModuleAccess() && module.getModuleConfig().getKeybind() == e.getKey()*/) {
                if (e.getKey() == InputConstants.KEY_ESCAPE) {
                    System.out.println("\"" + e.getKey() + "\"" + " PRESSED");
                }
                //module.getModuleConfig().setModuleEnabled(!module.getModuleConfig().isModuleEnabled(), true);
            }
       // }
    }

    public List<WorkerBind> getWorkerKeybinds() {
        return workerKeybinds;
    }
}
