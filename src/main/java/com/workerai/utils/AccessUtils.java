package com.workerai.utils;

import com.workerai.client.WorkerClient;
import com.workerai.client.handlers.hypixel.ScoreboardDetector;
import com.workerai.client.handlers.modules.ModulesHandler;
import com.workerai.client.modules.AbstractModule;
import com.workerai.client.modules.automine.AutomineModule;
import com.workerai.client.modules.fairy.FairyModule;
import com.workerai.client.modules.fairy.utils.CheckSurroundingFairies;

import java.util.List;

public class AccessUtils {
    private static final AccessUtils INSTANCE = new AccessUtils();
    private final CheckSurroundingFairies checkSurroundingFairies = new CheckSurroundingFairies();

    public ScoreboardDetector getScoreboardDetector() {
        return WorkerClient.getInstance().getWorkerHandler().getServerDetector().getScoreboardDetector();
    }

    public CheckSurroundingFairies getSurroundingFairies() {
        return checkSurroundingFairies;
    }

    public ModulesHandler getModuleHandler() {
        return WorkerClient.getInstance().getWorkerHandler().getModuleHandler();
    }

    public List<AbstractModule> getModules() {
        return WorkerClient.getInstance().getWorkerHandler().getModuleHandler().getModules();
    }

    public AutomineModule getAutomineModule() {
        return WorkerClient.getInstance().getWorkerHandler().getModuleHandler().getModule("Automine");
    }

    public FairyModule getFairyModule() {
        return WorkerClient.getInstance().getWorkerHandler().getModuleHandler().getModule("Fairy");
    }

    public static AccessUtils getInstance() {
        return INSTANCE;
    }
}
