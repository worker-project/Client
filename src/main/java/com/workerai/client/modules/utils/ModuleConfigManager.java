package com.workerai.client.modules.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.workerai.client.WorkerClient;
import com.workerai.client.handlers.keybinds.WorkerBind;
import com.workerai.client.modules.AbstractModule;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.Charsets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;

public class ModuleConfigManager {
    private final File configDir;
    private final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().registerTypeAdapter(WorkerBind.class, new ModuleConfigWriter()).create();
    private final String ext = ".config.json";

    public ModuleConfigManager() throws IOException {
        configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        for(AbstractModule module : WorkerClient.getInstance().getHandlersManager().getWorkerScripts().getModules()) {
            File configFile = new File(configDir, module.getModuleName().toLowerCase() + ext);
            if (configFile.exists() && configFile.isFile())
                return;
            if(!configFile.getParentFile().exists())
                if(!configFile.getParentFile().mkdirs()) {
                    LogUtils.getLogger().warn("Cannot create configuration folder!");
                    return;
                }

            if (!configFile.createNewFile()) {
                LogUtils.getLogger().warn("Cannot create " + module.getModuleName() + "'s config file!");
                return;
            }
            LogUtils.getLogger().info("Creating " + module.getModuleName() + "'s config file.");
            setConfig(module, module.getModuleConfig());
        }
    }

    public AbstractModuleConfig getConfig(AbstractModule module) {
        try {
            LogUtils.getLogger().info("Reading " + module.getModuleName() + "'s config file.");
            return gson.fromJson(
                    Files.readString(new File(configDir, module.getModuleName().toLowerCase() + ext).toPath(), Charsets.UTF_8),
                    (Type) module.getModuleConfigClass()
            );
        } catch (IOException e) {
            LogUtils.getLogger().error("Cannot retrieve " + module.getModuleName() + "'s config file!", e);
            return null;
        }
    }

    public void setConfig(AbstractModule module, AbstractModuleConfig config) {
        try {
            LogUtils.getLogger().info("Updating \u001B[33m" + module.getModuleName() + "\u001B[0m's config file.");
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(configDir, module.getModuleName().toLowerCase() + ext )));
            writer.write(gson.toJson(config));
            writer.flush();
            writer.close();
            module.onModuleConfigChange(config);
        } catch (IOException e) {
            LogUtils.getLogger().error("Can't update " + module.getModuleName() + "'s config!", e);
        }
    }
}
