package com.workerai.client.utils;

import com.workerai.client.WorkerClient;
import com.workerai.client.modules.AbstractModule;
import com.workerai.utils.ChatUtils;
import com.workerai.utils.RandomizerUtils;

public class ReconnectUtils {
    private static int currentServerID;

    public static void connectToServer() {
        AbstractModule module = WorkerClient.getInstance().getWorkerHandler().getModuleHandler().getActiveModule();
        if (module != null && module.getModuleConfig().isModuleEnabled() && module.getModuleConfig().isAutoReconnect()) {
            switch (currentServerID) {
                case 0:
                    ChatUtils.sendGuiMessage(
                            String.format("§5\n%s\n%s\n",
                                    "Detected Hypixel Lobby...",
                                    "Reconnecting to Skyblock Lobby!"
                            )
                    );
                    RandomizerUtils.waitBeforeWithExecute("/skyblock");
                    break;
                case 1:
                    ChatUtils.sendGuiMessage(
                            String.format("§5\n%s\n%s\n",
                                    "Detected Skyblock Lobby...",
                                    "Reconnecting to Skyblock Island!"
                            )
                    );
                    RandomizerUtils.waitBeforeWithExecute("/warp home");
                    break;
                case 2:
                    ChatUtils.sendGuiMessage(
                            String.format("§5%s\n%s\n",
                                    "Detected Skyblock Island...",
                                    "Done!"
                            )
                    );
                    break;
                case 3:
                    ChatUtils.sendGuiMessage(
                            String.format("§5\n%s\n%s\n",
                                    "Detected Skyblock Limbo...",
                                    "Reconnecting to Hypixel Lobby!"
                            )
                    );
                    RandomizerUtils.waitBeforeWithExecute("/lobby");
                    break;
                default:
                    ChatUtils.sendGuiMessage(
                            String.format("§5\n%s\n%s\n",
                                    "Detected unknown server...",
                                    "Reconnecting to Skyblock Lobby!"
                            )
                    );
                    RandomizerUtils.waitBeforeWithExecute("/skyblock");
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
