package com.workerai.client.modules.fairy.utils;

import com.workerai.client.WorkerClient;
import com.workerai.client.modules.fairy.FairyModule;
import com.workerai.client.modules.fairy.config.FairyConfig;
import net.minecraft.core.BlockPos;

public class FairyData {
    private BlockPos fairyPosition;
    private String fairyLocation;
    private boolean fairyUnlocked;
    private int[] fairyColor;

    public FairyData(BlockPos fairyPosition, String fairyLocation, boolean fairyUnlocked) {
        this.setFairyPosition(fairyPosition);
        this.setFairyLocation(fairyLocation);
        this.setFairyUnlocked(fairyUnlocked);
    }

    public BlockPos getFairyPosition() {
        return fairyPosition;
    }

    public void setFairyPosition(BlockPos fairyPosition) {
        this.fairyPosition = fairyPosition;
    }

    public int[] getFairyColor() {
        return fairyColor;
    }

    public String getFairyLocation() {
        return fairyLocation;
    }

    public void setFairyLocation(String fairyLocation) {
        this.fairyLocation = fairyLocation;
    }

    public boolean isFairyUnlocked() {
        return fairyUnlocked;
    }

    public void setFairyUnlocked(boolean fairyUnlocked) {
        this.fairyUnlocked = fairyUnlocked;

        if (!this.fairyUnlocked) {
            fairyColor = new int[]{255, 0, 0};
        } else {
            fairyColor = new int[]{0, 255, 0};
        }
    }

    public static boolean getFairyExistAtPosition(BlockPos pFairyPosition) {
        FairyConfig fairyConfig = WorkerClient.getInstance().getHandlersManager().getWorkerScripts().<FairyModule>getModule("Fairy").getModuleConfig();
        for (FairyData fairyData : fairyConfig.getDisplayedFairies()) {
            if (fairyData.getFairyPosition().equals(pFairyPosition)) {
                return true;
            }
        }
        return false;
    }
}
