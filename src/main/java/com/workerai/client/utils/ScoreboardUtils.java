package com.workerai.client.utils;

import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardUtils {
    public static List<String> getSidebarScores(Scoreboard scoreboard) {
        List<String> found = new ArrayList<>();
        Objective sidebar = scoreboard.getDisplayObjective(1);
        if (sidebar != null) {
            List<Score> scores = new ArrayList<>(scoreboard.getPlayerScores(sidebar));
            scores.sort(Comparator.comparingInt(Score::getScore));
            found = scores.stream()
                    .filter(score -> score.getObjective().getName().equals(sidebar.getName()))
                    .map(score -> score.getOwner() + getSuffixFromContainingTeam(scoreboard, score.getOwner()))
                    .collect(Collectors.toList());
        }
        return found;
    }

    private static String getSuffixFromContainingTeam(Scoreboard scoreboard, String member) {
        String suffix = null;
        for (PlayerTeam team : scoreboard.getPlayerTeams()) {
            if (team.getPlayers().contains(member)) {
                suffix = team.getName();
                break;
            }
        }
        return (suffix == null ? "" : suffix);
    }
}
