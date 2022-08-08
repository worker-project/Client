package com.workerai.client;

import com.workerai.client.handlers.hypixel.ServerDetector;
import com.workerai.client.handlers.hypixel.ServerReconnect;
import com.workerai.client.handlers.keybinds.KeyboardHandler;
import com.workerai.event.EventBus;

public class Handlers {
    /*private final ModulesHandler modulesHandler = new ModulesHandler();*/
    private final KeyboardHandler keyboardHandler = new KeyboardHandler();

    public Handlers() {
        //modulesHandler.add(new AutomineModule());
    }

    public void registerHandlers() {
        //EventBus.INSTANCE.register(new TickHandler());
        EventBus.INSTANCE.register(new ServerDetector());
        EventBus.INSTANCE.register(new ServerReconnect());
        //EventBus.INSTANCE.register(modulesHandler);

        EventBus.INSTANCE.register(keyboardHandler);
    }

    /*public ModulesHandler getWorkerScripts() {
        return modulesHandler;
    }*/

    public KeyboardHandler getKeyboardHandler() {
        return keyboardHandler;
    }
}
