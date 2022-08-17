package com.workerai.client;

import com.mojang.logging.LogUtils;
import com.workerai.client.modules.utils.ModuleConfigManager;
import com.workerai.utils.api.TokenResponse;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

import java.io.IOException;

public class WorkerClient {
    private static WorkerClient INSTANCE;
    private final Handlers handlersManager;

    private ModuleConfigManager moduleConfig;
    private TokenResponse tokenResponse;

    public WorkerClient(String[] userData) {
        INSTANCE = this;

        checkTokenAccess(userData);

        this.handlersManager = new Handlers();
        this.handlersManager.registerHandlers();

        try {
            this.moduleConfig = new ModuleConfigManager();
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't create config file", e);
        }
    }

    private void checkTokenAccess(String[] userData) {
        if(checkForDevEnvironment()) {
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

    public static WorkerClient getInstance() {
        return INSTANCE;
    }

    public Handlers getHandlersManager() {
        return handlersManager;
    }
}
