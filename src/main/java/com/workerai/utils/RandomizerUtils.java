package com.workerai.utils;

import com.mojang.logging.LogUtils;
import com.workerai.client.handlers.hypixel.ServerDetector;
import net.minecraft.client.Minecraft;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RandomizerUtils {
    public static void waitBefore() {
        Random random = new Random();
        int result = random.nextInt(5500 - 2000) + 2000;
        try {
            Thread.sleep(result);
        } catch (InterruptedException ignored) {
        }
    }

    public static void waitBeforeWithExecute(String command) {
        Random random = new Random();
        int result = random.nextInt(5500 - 2000) + 2000;
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(result);
                executeCommand(command);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void executeCommand(String command) {
        if (ServerDetector.getInstance().isInHypixel()) {
            try {
                Minecraft.getInstance().player.chat(command);
            } catch (NullPointerException ignored) {
                LogUtils.getLogger().warn("Hypixel not detected, aborting command execution!");
            }
        }
    }
}

