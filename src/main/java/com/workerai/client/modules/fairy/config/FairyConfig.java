package com.workerai.client.modules.fairy.config;

import com.workerai.client.modules.fairy.utils.FairyData;
import com.workerai.client.modules.fairy.utils.FairyPositions;
import com.workerai.client.modules.utils.AbstractModuleConfig;

import java.util.ArrayList;
import java.util.List;

public class FairyConfig extends AbstractModuleConfig {
    private final List<FairyData> missingFairies;
    private final List<FairyData> collectedFairies;
    private final List<FairyData> displayedFairies;
    private boolean fairyModuleEnabled;
    private int keyBind;

    private boolean hubFairyEnabled;
    private boolean dungeonFairyEnabled;
    private boolean miningOneFairyEnabled;
    private boolean miningTwoFairyEnabled;
    private boolean farmingFairyEnabled;
    private boolean winterFairyEnabled;

    public FairyConfig(int keyBind, List<FairyData> missingFairies, List<FairyData> collectedFairies, List<FairyData> displayedFairies) {
        this.fairyModuleEnabled = false;

        this.missingFairies = missingFairies;
        this.collectedFairies = collectedFairies;
        this.displayedFairies = displayedFairies;

        this.keyBind = keyBind;
    }

    public FairyConfig() {
        FairyConfig config = (FairyConfig) getDefaultConfig();

        this.fairyModuleEnabled = false;

        this.missingFairies = config.getMissingFairies();
        this.collectedFairies = config.getCollectedFairies();
        this.displayedFairies = config.getDisplayedFairies();

        this.keyBind = config.keyBind;
    }

    public void addDisplayedFairy(FairyData fairyData) {
        displayedFairies.add(fairyData);
    }

    public void addMissingFairy(FairyData fairyData) {
        missingFairies.add(fairyData);
    }

    public void addCollectedFairy(FairyData fairyData) {
        collectedFairies.add(fairyData);
    }

    public void removeDisplayedFairy(FairyData fairyData) {
        displayedFairies.remove(fairyData);
    }

    public void removeMissingFairy(FairyData fairyData) {
        missingFairies.remove(fairyData);
    }

    public void removeCollectedFairy(FairyData fairyData) {
        collectedFairies.remove(fairyData);
    }

    public void addDisplayedFairy(List<FairyData> fairiesData) {
        for (FairyData fairyData : fairiesData) {
            addDisplayedFairy(fairyData);
        }
    }

    public boolean addMissingFairy(List<FairyData> fairiesData) {
        for (FairyData fairyData : fairiesData) {
            addMissingFairy(fairyData);
        }
        return true;
    }

    public void addCollectedFairies(List<FairyData> fairiesData) {
        for (FairyData fairyData : fairiesData) {
            addCollectedFairy(fairyData);
        }
    }

    public void removeDisplayedFairies(List<FairyData> fairiesData) {
        for (FairyData fairyData : fairiesData) {
            removeDisplayedFairy(fairyData);
        }
    }

    public void removeMissingFairies(List<FairyData> fairiesData) {
        for (FairyData fairyData : fairiesData) {
            removeMissingFairy(fairyData);
        }
    }

    public void removeCollectedFairies(List<FairyData> fairiesData) {
        for (FairyData fairyData : fairiesData) {
            removeCollectedFairy(fairyData);
        }
    }

    public List<FairyData> getCollectedFairies() {
        return collectedFairies;
    }

    public List<FairyData> getMissingFairies() {
        return missingFairies;
    }

    public List<FairyData> getDisplayedFairies() {
        return displayedFairies;
    }

    @Override
    public int getKeybind() {
        return keyBind;
    }

    @Override
    public void setKeybind(int keyBind) {
        this.keyBind = keyBind;
    }

    @Override
    public boolean isModuleEnabled() {
        return fairyModuleEnabled;
    }

    @Override
    public void setModuleEnabled(boolean active, boolean debug) {
        this.fairyModuleEnabled = active;
        if (debug) displayDebugMessage("Module", active, "Fairy");
    }

    @Override
    public AbstractModuleConfig getDefaultConfig() {
        return new FairyConfig(999, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public boolean isHubFairyEnabled() {
        return hubFairyEnabled;
    }

    public boolean isDungeonFairyEnabled() {
        return dungeonFairyEnabled;
    }

    public boolean isMiningOneFairyEnabled() {
        return miningOneFairyEnabled;
    }

    public boolean isMiningTwoFairyEnabled() {
        return miningTwoFairyEnabled;
    }

    public boolean isWinterFairyEnabled() {
        return winterFairyEnabled;
    }

    public void setHubFairyEnabled(boolean active, boolean debug) {
        this.hubFairyEnabled = active;
        if (debug) displayDebugMessage("Fairy", active, "Hub");

        if (this.isHubFairyEnabled()) {
            System.out.println("Added");
            addDisplayedFairy(FairyPositions.HUB);
        } else {
            System.out.println("Removed");
            removeDisplayedFairies(FairyPositions.HUB);
        }
    }

    public void setDungeonFairyEnabled(boolean active, boolean debug) {
        this.dungeonFairyEnabled = active;
        if (debug) displayDebugMessage("Fairy", active, "Dungeon");

        if (this.isDungeonFairyEnabled()) {
            addDisplayedFairy(FairyPositions.DUNGEON);
        } else {
            removeDisplayedFairies(FairyPositions.DUNGEON);
        }
    }

    public void setMiningOneFairyEnabled(boolean active, boolean debug) {
        this.miningOneFairyEnabled = active;
        if (debug) displayDebugMessage("Fairy", active, "Mining 1");

        if (this.isMiningOneFairyEnabled()) {
            addDisplayedFairy(FairyPositions.MINING_1);
        } else {
            removeDisplayedFairies(FairyPositions.MINING_1);
        }
    }

    public void setMiningTwoFairyEnabled(boolean active, boolean debug) {
        this.miningTwoFairyEnabled = active;
        if (debug) displayDebugMessage("Fairy", active, "Mining 2");

        if (this.isMiningTwoFairyEnabled()) {
            addDisplayedFairy(FairyPositions.MINING_2);
        } else {
            removeDisplayedFairies(FairyPositions.MINING_2);
        }
    }

    public void setWinterFairyEnabled(boolean active, boolean debug) {
        this.winterFairyEnabled = active;
        if (debug) displayDebugMessage("Fairy", active, "Winter");

        if (this.isWinterFairyEnabled()) {
            addDisplayedFairy(FairyPositions.WINTER);
        } else {
            removeDisplayedFairies(FairyPositions.WINTER);
        }
    }

    @Override
    public boolean isAutoReconnect() {
        return false;
    }

    @Override
    public boolean isAutoDrop() {
        return false;
    }

    @Override
    public boolean isAutoCraft() {
        return false;
    }

    @Override
    public void setAutoReconnect(boolean active, boolean debug) {

    }

    @Override
    public void setAutoDrop(boolean active, boolean debug) {

    }

    @Override
    public void setAutoCraft(boolean active, boolean debug) {

    }
}
