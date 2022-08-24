package com.workerai.client.modules.fairy.config;

import com.workerai.client.modules.fairy.utils.FairyPositions;
import com.workerai.client.modules.utils.AbstractModuleConfig;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class FairyConfig extends AbstractModuleConfig {
    private boolean fairyModuleEnabled;
    private boolean hubDisplay;
    private boolean winterDisplay;
    private boolean dungeonDisplay;
    private boolean farmingDisplay;
    private boolean crimsonDisplay;
    private boolean foragingDisplay;
    private boolean combatOneDisplay;
    private boolean combatThreeDisplay;
    private boolean goldMineDisplay;
    private boolean deepCavernsDisplay;
    private boolean miningThreeDisplay;
    private int keyBind;

    private final List<BlockPos> allFairies;
    private final List<BlockPos> collectedFairies;

    public enum LIST {
        COLLECTED,
        ALL,
    }

    public enum LOCATION {
        HUB,
        WINTER,
        DUNGEON,
        FORAGING,
        CRIMSON,
        FARMING,
        COMBAT_1,
        COMBAT_3,
        GOLD_MINE,
        DEEP_CAVERNS,
        MINING_3
    }

    public FairyConfig(int keyBind, boolean huDisplay, boolean winterDisplay, boolean dungeonDisplay, List<BlockPos> allFairies, List<BlockPos> collectedFairies) {
        this.fairyModuleEnabled = false;

        this.allFairies = allFairies;
        this.collectedFairies = collectedFairies;

        this.hubDisplay = huDisplay;
        this.dungeonDisplay = dungeonDisplay;
        this.winterDisplay = winterDisplay;

        this.keyBind = keyBind;
    }

    public FairyConfig() {
        FairyConfig config = getDefaultConfig();

        this.fairyModuleEnabled = false;

        this.allFairies = config.allFairies;
        this.collectedFairies = config.collectedFairies;

        this.hubDisplay = config.hubDisplay;
        this.dungeonDisplay = config.dungeonDisplay;
        this.winterDisplay = config.winterDisplay;

        this.keyBind = config.keyBind;
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

    public void setDisplay(boolean active, LOCATION location, boolean debug) {
        switch (location) {
            case HUB -> this.hubDisplay = setDisplayHelper(active, debug, FairyPositions.HUB, "Hub");

            case WINTER -> this.winterDisplay = setDisplayHelper(active, debug, FairyPositions.WINTER, "Winter");

            case DUNGEON -> this.dungeonDisplay = setDisplayHelper(active, debug, FairyPositions.DUNGEON, "Dungeon");

            case CRIMSON -> this.crimsonDisplay = setDisplayHelper(active, debug, FairyPositions.CRIMSON, "Crimson");
            case FORAGING -> this.foragingDisplay = setDisplayHelper(active, debug, FairyPositions.FORAGING, "Foraging");
            case FARMING -> this.farmingDisplay = setDisplayHelper(active, debug, FairyPositions.FARMING, "Farming");

            case COMBAT_1 -> this.combatOneDisplay = setDisplayHelper(active, debug, FairyPositions.COMBAT_1, "Combat One");
            case COMBAT_3 -> this.combatThreeDisplay = setDisplayHelper(active, debug, FairyPositions.COMBAT_3, "Combat Three");

            case GOLD_MINE -> this.goldMineDisplay = setDisplayHelper(active, debug, FairyPositions.GOLD_MINE, "Gold Mine");
            case DEEP_CAVERNS -> this.deepCavernsDisplay = setDisplayHelper(active, debug, FairyPositions.DEEP_CAVERNS, "Deep Caverns");
            case MINING_3 -> this.miningThreeDisplay = setDisplayHelper(active, debug, FairyPositions.MINING_3, "Mining Three");
        }
    }

    private boolean setDisplayHelper(boolean active, boolean debug, List<BlockPos> fairyPositions, String fairyLocation) {
        if (active) {
            this.addFairies(fairyPositions, LIST.ALL);
        } else {
            this.removeFairies(fairyPositions, LIST.ALL);
        }
        if (debug) displayDebugMessage("Fairy", active, fairyLocation + " Display");
        return active;
    }

    public boolean isDisplay(LOCATION location) {
        switch (location) {
            case HUB -> { return this.hubDisplay; }
            case WINTER -> {
                return this.winterDisplay;
            }
            case DUNGEON -> {
                return this.dungeonDisplay;
            }
            case FORAGING -> {
                return this.foragingDisplay;
            }
            case FARMING -> {
                return this.farmingDisplay;
            }
            case CRIMSON -> {
                return this.crimsonDisplay;
            }
            case COMBAT_1 -> {
                return this.combatOneDisplay;
            }
            case COMBAT_3 -> {
                return this.combatThreeDisplay;
            }
            case GOLD_MINE -> {
                return this.goldMineDisplay;
            }
            case DEEP_CAVERNS -> {
                return this.deepCavernsDisplay;
            }
            case MINING_3 -> {
                return this.miningThreeDisplay;
            }
        }
        return false;
    }

    public void addFairies(List<BlockPos> fairiesPos, LIST list) {
        switch (list) {
            case ALL:
                for (BlockPos fairyPos : fairiesPos) {
                    addAllFairies(fairyPos);
                }
                break;
            case COLLECTED:
                for (BlockPos fairyPos : fairiesPos) {
                    addCollectedFairies(fairyPos);
                }
                break;
        }
    }

    public void removeFairies(List<BlockPos> fairiesPos, LIST list) {
        switch (list) {
            case ALL:
                for (BlockPos fairyPos : fairiesPos) {
                    removeAllFairies(fairyPos);
                }
                break;
            case COLLECTED:
                for (BlockPos fairyPos : fairiesPos) {
                    removeCollectedFairies(fairyPos);
                }
                break;
        }
    }

    public void addCollectedFairies(BlockPos fairyPos) {
        if (collectedFairies.contains(fairyPos)) return;
        collectedFairies.add(fairyPos);
    }

    void addAllFairies(BlockPos fairyPos) {
        if (allFairies.contains(fairyPos)) return;
        allFairies.add(fairyPos);
    }

    void removeCollectedFairies(BlockPos fairyPos) {
        if (!collectedFairies.contains(fairyPos)) return;
        collectedFairies.remove(fairyPos);
    }

    void removeAllFairies(BlockPos fairyPos) {
        if (!allFairies.contains(fairyPos)) return;
        ;
        allFairies.remove(fairyPos);
    }

    public boolean isAlreadyListed(List<BlockPos> fairiesPos, LIST list) {
        switch (list) {
            case ALL:
                for (BlockPos fairyPos : fairiesPos) {
                    if (allFairies.contains(fairyPos)) return true;
                }
                break;
            case COLLECTED:
                for (BlockPos fairyPos : fairiesPos) {
                    if (collectedFairies.contains(fairyPos)) return true;
                }
                break;
        }
        return false;
    }

    public List<BlockPos> getAllFairies() {
        return allFairies;
    }

    public List<BlockPos> getCollectedFairies() {
        return collectedFairies;
    }

    @Override
    public FairyConfig getDefaultConfig() {
        return new FairyConfig(999, false, false, false, new ArrayList<>(), new ArrayList<>());
    }
}
