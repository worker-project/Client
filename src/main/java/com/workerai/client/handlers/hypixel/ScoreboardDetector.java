package com.workerai.client.handlers.hypixel;

import com.workerai.client.utils.ScoreboardUtils;
import com.workerai.event.EventBus;
import com.workerai.event.network.server.ReconnectEvent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.Collection;
import java.util.List;

public class ScoreboardDetector {
    public static class ServerType {
        public static int HYPIXEL_LOBBY = 0;
        public static int SKYBLOCK_LOBBY = 1;
        public static int SKYBLOCK_ISLAND = 2;
        public static int HYPIXEL_LIMBO = 3;
        public static int OTHER = 4;
    }

    private Scoreboard scoreboard;
    private static String currentServer;

    public void refreshCurrentServer(boolean afk) {
        Collection<Objective> scoreObjective = scoreboard.getObjectives();
        if (scoreObjective.size() > 0) {
            Objective objective = scoreObjective.iterator().next();
            if (objective != null) {
                String line = scoreboard.getObjective(objective.getName()).getDisplayName().getString();
                if (line != null) {
                    currentServer = line;
                } else {
                    return;
                }
            }
        }

        if (currentServer.contains("SKYBLOCK")) currentServer = "SKYBLOCK";

        if (afk) {
            EventBus.INSTANCE.post(new ReconnectEvent(ServerType.HYPIXEL_LIMBO, "LIMBO"));
            return;
        }

        switch (currentServer) {
            case "HYPIXEL" -> {
                EventBus.INSTANCE.post(new ReconnectEvent(ServerType.HYPIXEL_LOBBY, currentServer));
                return;
            }
            case "SKYBLOCK" -> {
                if (isInIsland()) {
                    EventBus.INSTANCE.post(new ReconnectEvent(ServerType.SKYBLOCK_ISLAND, currentServer));
                } else {
                    EventBus.INSTANCE.post(new ReconnectEvent(ServerType.SKYBLOCK_LOBBY, currentServer));
                }
                return;
            }
        }
        EventBus.INSTANCE.post(new ReconnectEvent(ServerType.OTHER, currentServer));
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    private boolean isInIsland() {
        List<String> currentScoreboard = ScoreboardUtils.getSidebarScores(scoreboard);
        return currentScoreboard.size() == 12;
    }
}
