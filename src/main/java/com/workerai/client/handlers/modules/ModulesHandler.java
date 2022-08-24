package com.workerai.client.handlers.modules;

import com.workerai.client.modules.AbstractModule;

import java.util.ArrayList;
import java.util.List;

public class ModulesHandler {
    private final List<AbstractModule> AbstractModules = new ArrayList<>();

    public void add(AbstractModule module) {
        AbstractModules.add(module);
    }

    public void remove(AbstractModule module) {
        AbstractModules.remove(module);
    }

    public List<AbstractModule> getModules() {
        return AbstractModules;
    }

    public <T extends AbstractModule> T getModule(String name) {
        for (AbstractModule module : AbstractModules) {
            if (module.getModuleName().equalsIgnoreCase(name)) {
                return (T) module;
            }
        }
        return null;
    }

    public AbstractModule getActiveModule() {
        for (AbstractModule module : AbstractModules) {
            if (module.getModuleConfig().isModuleEnabled()) {
                return module;
            }
        }
        return null;
    }
}
