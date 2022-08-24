package com.workerai.client.handlers.keybinds;

import com.workerai.client.WorkerClient;
import com.workerai.client.handlers.hypixel.ServerDetector;
import com.workerai.client.modules.AbstractModule;
import com.workerai.event.interact.KeyPressedEvent;
import com.workerai.event.utils.InvokeEvent;

import java.util.ArrayList;
import java.util.List;

public class KeyboardHandler {
    private final List<WorkerBind> workerKeybinds = new ArrayList<>();

    @InvokeEvent
    public void onKeyPressed(KeyPressedEvent e) {
        for (AbstractModule module : WorkerClient.getInstance().getWorkerHandler().getModuleHandler().getModules()) {
            if (ServerDetector.getInstance().isInHypixel()/* && module.hasModuleAccess() && module.getModuleConfig().getKeybind() == e.getKey()*/) {
                if (e.getKey() == module.getModuleConfig().getKeybind()) {
                    module.setOtherModulesDisabled(true);
                    module.getModuleConfig().setModuleEnabled(!module.getModuleConfig().isModuleEnabled(), true);
                    module.setModuleConfig(module.getModuleConfig());
                }
            }
        }
    }

    public List<WorkerBind> getWorkerKeybinds() {
        return workerKeybinds;
    }
}
