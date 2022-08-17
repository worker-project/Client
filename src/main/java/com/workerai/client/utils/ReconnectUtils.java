package com.workerai.client.utils;

import com.workerai.client.WorkerClient;
import com.workerai.client.modules.AbstractModule;
import com.workerai.utils.ChatDebug;
import com.workerai.utils.HumanRandomizer;

public class ReconnectUtils {
    private static int currentServerID;

    public static void connectToServer() {
        AbstractModule module = WorkerClient.getInstance().getHandlersManager().getWorkerScripts().getActiveModule();
        if (module != null && module.getModuleConfig().isModuleEnabled() && module.getModuleConfig().isAutoReconnect()) {
            switch (currentServerID) {
                case 0:
                    ChatDebug.sendGuiMessage(
                            String.format("§5\n%s\n%s\n",
                                    "Detected Hypixel Lobby...",
                                    "Reconnecting to Skyblock Lobby!"
                            )
                    );
                    HumanRandomizer.waitBeforeWithExecute("/skyblock");
                    break;
                case 1:
                    ChatDebug.sendGuiMessage(
                            String.format("§5\n%s\n%s\n",
                                    "Detected Skyblock Lobby...",
                                    "Reconnecting to Skyblock Island!"
                            )
                    );
                    HumanRandomizer.waitBeforeWithExecute("/warp home");
                    break;
                case 2:
                    ChatDebug.sendGuiMessage(
                            String.format("§5%s\n%s\n",
                                    "Detected Skyblock Island...",
                                    "Done!"
                            )
                    );
                    break;
                case 3:
                    ChatDebug.sendGuiMessage(
                            String.format("§5\n%s\n%s\n",
                                    "Detected Skyblock Limbo...",
                                    "Reconnecting to Hypixel Lobby!"
                            )
                    );
                    HumanRandomizer.waitBeforeWithExecute("/lobby");
                    break;
                default:
                    ChatDebug.sendGuiMessage(
                            String.format("§5\n%s\n%s\n",
                                    "Detected unknown server...",
                                    "Reconnecting to Skyblock Lobby!"
                            )
                    );
                    HumanRandomizer.waitBeforeWithExecute("/skyblock");
                    break;
            }
        }
    }

    public static void setCurrentServerID(int currentServerID) {
        ReconnectUtils.currentServerID = currentServerID;
    }

    public static int getCurrentServerID() {
        return currentServerID;
    }
}
