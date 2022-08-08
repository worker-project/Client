package com.workerai.client.utils;

import com.workerai.utils.ChatColor;
import com.workerai.utils.ChatDebug;
import com.workerai.utils.HumanRandomizer;

public class ReconnectUtils {
    private static int currentServerID;

    public static void connectToServer() {
        //IModule module = WorkerAI.getInstance().getHandlers().getWorkerScripts().getActiveModule();
        //if(module != null && module.getModuleConfig().isModuleEnabled() && module.getModuleConfig().isAutoReconnect()) {
        switch (currentServerID) {
            case 0:
                ChatDebug.sendGuiMessage(
                        " ",
                        ChatColor.DARK_PURPLE.getChar() + "Detected Hypixel Lobby",
                        ChatColor.DARK_PURPLE.getChar() + "Reconnecting to Skyblock Lobby",
                        " "
                );
                HumanRandomizer.waitBeforeWithExecute("/skyblock");
                break;
            case 1:
                ChatDebug.sendGuiMessage(
                        " ",
                        ChatColor.DARK_PURPLE.getChar() + "Detected Skyblock Lobby",
                        ChatColor.DARK_PURPLE.getChar() + "Reconnecting to Skyblock Island",
                        " "
                );
                HumanRandomizer.waitBeforeWithExecute("/warp home");
                break;
            case 2:
                ChatDebug.sendGuiMessage(
                        ChatColor.DARK_PURPLE.getChar() + "Detected Skyblock Island",
                        ChatColor.DARK_PURPLE.getChar() + "Done!",
                        " "
                );
                break;
            case 3:
                ChatDebug.sendGuiMessage(
                        ChatColor.DARK_PURPLE.getChar() + "Detected Hypixel Limbo",
                        ChatColor.DARK_PURPLE.getChar() + "Reconnecting to Hypixel Lobby"
                );
                HumanRandomizer.waitBeforeWithExecute("/lobby");
                break;
            default:
                ChatDebug.sendGuiMessage(
                        " ",
                        ChatColor.DARK_PURPLE.getChar() + "Detected unknown server",
                        ChatColor.DARK_PURPLE.getChar() + "Reconnecting to Skyblock Lobby",
                        " "
                );
                HumanRandomizer.waitBeforeWithExecute("/skyblock");
                break;
        }
    }
    //}

    public static void setCurrentServerID(int currentServerID) {
        ReconnectUtils.currentServerID = currentServerID;
    }

    public static int getCurrentServerID() {
        return currentServerID;
    }
}
