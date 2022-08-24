package com.workerai.client;

import com.workerai.client.modules.utils.ModuleConfigManager;
import com.workerai.utils.response.TokenResponse;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

public class WorkerClient {
    private static WorkerClient INSTANCE;
    private final WorkerHandler workerHandler;
    private final ModuleConfigManager moduleConfig;
    private TokenResponse tokenResponse;

    public WorkerClient(String[] userData) {
        INSTANCE = this;

        checkTokenAccess(userData);

        this.workerHandler = new WorkerHandler();
        this.workerHandler.registerHandlers();

        this.moduleConfig = new ModuleConfigManager();
        this.moduleConfig.disableModules();
    }

    private void checkTokenAccess(String[] userData) {
        if (checkForDevEnvironment()) {
            tokenResponse = new TokenResponse(true, true);
            return;
        }

        try {
            tokenResponse = TokenResponse.getTokenInformation(userData[0], userData[1]);
        } catch (TokenResponse.UserNotFoundException e) {
            CrashReport crashreport = CrashReport.forThrowable(e, "Token error");
            throw new ReportedException(crashreport);
        }
    }

    private boolean checkForDevEnvironment() {
        try {
            Class.forName("net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public ModuleConfigManager getModuleConfig() {
        return this.moduleConfig;
    }

    public TokenResponse getTokenResponse() {
        return this.tokenResponse;
    }


    public WorkerHandler getWorkerHandler() {
        return this.workerHandler;
    }

    public static WorkerClient getInstance() {
        return INSTANCE;
    }
}
