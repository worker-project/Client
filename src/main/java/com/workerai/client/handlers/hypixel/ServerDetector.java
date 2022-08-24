package com.workerai.client.handlers.hypixel;

import com.mojang.logging.LogUtils;
import com.workerai.event.EventBus;
import com.workerai.event.network.server.JoinHypixelEvent;
import com.workerai.event.network.server.LeaveHypixelEvent;
import com.workerai.event.network.server.ServerJoinEvent;
import com.workerai.event.network.server.ServerLeaveEvent;
import com.workerai.event.utils.InvokeEvent;
import com.workerai.event.world.EntityJoinWorldEvent;
import com.workerai.utils.MultithreadingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Scoreboard;

import java.util.Timer;
import java.util.TimerTask;

import static com.workerai.utils.ClientUtils.HYPIXEL_PATTERN;

public class ServerDetector {
    private static ServerDetector INSTANCE;
    private boolean detectedHypixel;
    private final ScoreboardDetector scoreboardDetector = new ScoreboardDetector();
    private Timer joinTimer;

    public ServerDetector() {
        INSTANCE = this;
    }

    @InvokeEvent
    public void onServerJoin(ServerJoinEvent event) {
        detectedHypixel = getMatcherIP(event.getServer());
        MultithreadingUtils.runAsync(() -> {
            int tries = 0;
            while (Minecraft.getInstance().player == null) {
                tries++;
                if (tries > 20 * 10) return;
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (detectedHypixel) {
                EventBus.INSTANCE.post(new JoinHypixelEvent(JoinHypixelEvent.ServerVerificationMethod.IP));
            }
        });
    }

    @InvokeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!isInHypixel()) return;
            if (!player.getUUID().toString().replaceAll("-", "").equals(Minecraft.getInstance().getUser().getUuid()))
                return;
            if (joinTimer != null) joinTimer.cancel();

            joinTimer = new Timer();
            joinTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            MultithreadingUtils.runAsync(() -> {
                                Scoreboard scoreboard = player.getScoreboard();
                                int tries = 0;
                                while (scoreboard.getObjectives().size() <= 0 && scoreboard.getPlayerScores(player.getDisplayName().getContents()).size() <= 0) {
                                    tries++;
                                    if (tries > 20 * 10) return;
                                    try {
                                        Thread.sleep(50L);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                scoreboardDetector.setScoreboard(scoreboard);
                                scoreboardDetector.refreshCurrentServer(false);
                            });
                        }
                    }, 1000
            );
        }
    }

    @InvokeEvent
    public void onServerLeave(ServerLeaveEvent event) {
        if (detectedHypixel) {
            detectedHypixel = false;
            EventBus.INSTANCE.post(new LeaveHypixelEvent(LeaveHypixelEvent.ServerVerificationMethod.IP));
        }
    }

    @InvokeEvent
    public void onHypixelJoin(JoinHypixelEvent e) {
        LogUtils.getLogger().info("\u001B[33mJoining Hypixel...\u001B[0m");
    }

    @InvokeEvent
    public void onHypixelLeave(LeaveHypixelEvent e) {
        LogUtils.getLogger().info("\u001B[33mLeaving Hypixel...\u001B[0m");
    }


    public static ServerDetector getInstance() {
        return INSTANCE;
    }

    public boolean isInHypixel() {
        return detectedHypixel;
    }

    public boolean getMatcherIP(String ip) {
        return HYPIXEL_PATTERN.matcher(ip).find();
    }

    public ScoreboardDetector getScoreboardDetector() {
        return scoreboardDetector;
    }
}
