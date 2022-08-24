package com.workerai.client;

import com.workerai.client.handlers.hypixel.ChatDetector;
import com.workerai.client.handlers.hypixel.ServerDetector;
import com.workerai.client.handlers.hypixel.ServerReconnect;
import com.workerai.client.handlers.keybinds.KeyboardHandler;
import com.workerai.client.handlers.modules.ModulesHandler;
import com.workerai.client.modules.automine.AutomineModule;
import com.workerai.client.modules.fairy.FairyModule;
import com.workerai.event.EventBus;

public class WorkerHandler {
    private final ModulesHandler modulesHandler = new ModulesHandler();
    private final KeyboardHandler keyboardHandler = new KeyboardHandler();
    private final ServerDetector serverDetector = new ServerDetector();

    public WorkerHandler() {
        modulesHandler.add(new AutomineModule());
        modulesHandler.add(new FairyModule());
    }

    public void registerHandlers() {
        EventBus.INSTANCE.register(serverDetector);
        EventBus.INSTANCE.register(new ServerReconnect());
        EventBus.INSTANCE.register(modulesHandler);
        EventBus.INSTANCE.register(keyboardHandler);

        EventBus.INSTANCE.register(new ChatDetector());
    }

    public ModulesHandler getModuleHandler() {
        return modulesHandler;
    }

    public KeyboardHandler getKeyboardHandler() {
        return keyboardHandler;
    }
    public ServerDetector getServerDetector() {
        return serverDetector;
    }
}
