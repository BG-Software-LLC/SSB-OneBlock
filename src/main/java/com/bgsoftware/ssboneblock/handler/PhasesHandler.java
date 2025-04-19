package com.bgsoftware.ssboneblock.handler;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.actions.Action;
import com.bgsoftware.ssboneblock.data.DataStore;
import com.bgsoftware.ssboneblock.lang.Message;
import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.ssboneblock.phases.PhaseData;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.ssboneblock.utils.Resources;
import com.bgsoftware.ssboneblock.utils.WorldUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PhasesHandler {

    private final Map<String, JsonArray> possibilities = new ConcurrentHashMap<>();

    private final OneBlockModule module;
    private final DataStore dataStore;
    private final PhaseData[] phaseData;

    public PhasesHandler(OneBlockModule module, DataStore dataStore) {
        this.module = module;
        this.dataStore = dataStore;
        phaseData = loadData();
    }

    public JsonArray getPossibilities(String possibilities) {
        return this.possibilities.getOrDefault(possibilities.toLowerCase(), new JsonArray());
    }

    @Nullable
    public PhaseData getPhaseData(IslandPhaseData islandPhaseData) {
        return getPhaseData(islandPhaseData.getPhaseLevel());
    }

    @Nullable
    public PhaseData getPhaseData(int phaseLevel) {
        return phaseLevel >= phaseData.length ? null : phaseData[phaseLevel];
    }

    public void runNextAction(Island island, @Nullable SuperiorPlayer superiorPlayer) {
        if (!canHaveOneBlock(island))
            return;

        IslandPhaseData islandPhaseData = this.dataStore.getPhaseData(island, true);

        if (islandPhaseData.getPhaseLevel() >= phaseData.length) {
            if (superiorPlayer != null)
                Message.NO_MORE_PHASES.send(superiorPlayer);
            return;
        }

        PhaseData phaseData = this.phaseData[islandPhaseData.getPhaseLevel()];
        Action action = phaseData.getAction(islandPhaseData.getPhaseBlock());

        Location oneBlockLocation = WorldUtils.getOneBlock(island);

        if (action == null) {
            int nextPhaseLevel = islandPhaseData.getPhaseLevel() + 1 < this.phaseData.length ?
                    islandPhaseData.getPhaseLevel() + 1 : module.getSettings().phasesLoop ? 0 : -1;
            runNextActionTimer(island, superiorPlayer, oneBlockLocation, phaseData, nextPhaseLevel);
            return;
        }

        Optional.ofNullable(NextPhaseTimer.getTimer(island)).ifPresent(nextPhaseTimer -> {
            nextPhaseTimer.setRunFinishCallback(false);
            nextPhaseTimer.cancel();
        });

        action.run(oneBlockLocation, island, superiorPlayer);

        IslandPhaseData newPhaseData = this.dataStore.getPhaseData(island, false);

        if (newPhaseData == islandPhaseData)
            this.dataStore.setPhaseData(island, islandPhaseData.nextBlock());

        Message.PHASE_PROGRESS.send(superiorPlayer,
                islandPhaseData.getPhaseBlock() * 100 / phaseData.getActionsSize(),
                islandPhaseData.getPhaseBlock(),
                phaseData.getActionsSize());

        // We check for last phase here as well.
        if (module.getSettings().phasesLoop && islandPhaseData.getPhaseBlock() + 1 == phaseData.getActionsSize() &&
                islandPhaseData.getPhaseLevel() + 1 == this.phaseData.length)
            runNextActionTimer(island, superiorPlayer, oneBlockLocation, phaseData, 0);
    }

    private void runNextActionTimer(Island island, @Nullable SuperiorPlayer superiorPlayer, Location oneBlockLocation,
                                    PhaseData phaseData, int nextPhaseLevel) {
        if (NextPhaseTimer.getTimer(island) == null) {
            oneBlockLocation.getBlock().setType(Material.BEDROCK);
            if (nextPhaseLevel >= 0) {
                new NextPhaseTimer(island, phaseData.getNextPhaseCooldown(),
                        () -> setPhaseLevel(island, nextPhaseLevel, superiorPlayer));
            }
        }
    }

    public boolean setPhaseLevel(Island island, int phaseLevel, @Nullable SuperiorPlayer superiorPlayer) {
        if (phaseLevel >= phaseData.length)
            return false;

        IslandPhaseData islandPhaseData = new IslandPhaseData(phaseLevel, 0);
        this.dataStore.setPhaseData(island, islandPhaseData);

        runNextAction(island, superiorPlayer);

        return true;
    }

    public boolean setPhaseBlock(Island island, int phaseBlock, @Nullable SuperiorPlayer superiorPlayer) {
        IslandPhaseData islandPhaseData = this.dataStore.getPhaseData(island, true);
        PhaseData phaseData = this.phaseData[islandPhaseData.getPhaseLevel()];

        if (phaseData.getAction(phaseBlock) == null)
            return false;

        this.dataStore.setPhaseData(island, new IslandPhaseData(islandPhaseData.getPhaseLevel(), phaseBlock));
        runNextAction(island, superiorPlayer);

        return true;
    }

    public boolean canHaveOneBlock(Island island) {
        return !island.isSpawn() && (module.getSettings().whitelistedSchematics.isEmpty() ||
                module.getSettings().whitelistedSchematics.contains(island.getSchematicName().toUpperCase()));
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private PhaseData[] loadData() {
        File phasesFolder = new File(module.getModuleFolder(), "phases");
        File possibilitiesFolder = new File(module.getModuleFolder(), "possibilities");

        if (!phasesFolder.exists()) {
            phasesFolder.mkdirs();
            Resources.saveResource("phases/plains-phase.json");
            Resources.saveResource("phases/underground-phase.json");
            Resources.saveResource("phases/snow-phase.json");
            Resources.saveResource("phases/ocean-phase.json");
            Resources.saveResource("phases/jungle-phase.json");
            Resources.saveResource("phases/red-desert-phase.json");
            Resources.saveResource("phases/nether-phase.json");
            Resources.saveResource("phases/idyll-phase.json");
            Resources.saveResource("phases/desolate-phase.json");
            Resources.saveResource("phases/end-phase.json");
        }

        if (!possibilitiesFolder.exists()) {
            possibilitiesFolder.mkdirs();
            Resources.saveResource("possibilities/plains-blocks.json");
            Resources.saveResource("possibilities/plains-chests.json");
            Resources.saveResource("possibilities/plains-mobs.json");
            Resources.saveResource("possibilities/underground-blocks.json");
            Resources.saveResource("possibilities/underground-chests.json");
            Resources.saveResource("possibilities/underground-mobs.json");
            Resources.saveResource("possibilities/snow-blocks.json");
            Resources.saveResource("possibilities/snow-chests.json");
            Resources.saveResource("possibilities/snow-mobs.json");
            Resources.saveResource("possibilities/ocean-blocks.json");
            Resources.saveResource("possibilities/ocean-chests.json");
            Resources.saveResource("possibilities/ocean-mobs.json");
            Resources.saveResource("possibilities/jungle-blocks.json");
            Resources.saveResource("possibilities/jungle-chests.json");
            Resources.saveResource("possibilities/jungle-mobs.json");
            Resources.saveResource("possibilities/red-desert-blocks.json");
            Resources.saveResource("possibilities/red-desert-chests.json");
            Resources.saveResource("possibilities/red-desert-mobs.json");
            Resources.saveResource("possibilities/nether-blocks.json");
            Resources.saveResource("possibilities/nether-chests.json");
            Resources.saveResource("possibilities/nether-mobs.json");
            Resources.saveResource("possibilities/idyll-blocks.json");
            Resources.saveResource("possibilities/idyll-chests.json");
            Resources.saveResource("possibilities/idyll-mobs.json");
            Resources.saveResource("possibilities/desolate-blocks.json");
            Resources.saveResource("possibilities/desolate-chests.json");
            Resources.saveResource("possibilities/desolate-mobs.json");
            Resources.saveResource("possibilities/end-blocks.json");
            Resources.saveResource("possibilities/end-chests.json");
            Resources.saveResource("possibilities/end-mobs.json");
            Resources.saveResource("possibilities/superchest.json");
            Resources.saveResource("possibilities/rarechest.json");
        }

        File[] possibilityFiles = possibilitiesFolder.listFiles();

        assert possibilityFiles != null;

        for (File possibilityFile : possibilityFiles) {
            try {
                JsonArray jsonArray = JsonUtils.parseFile(possibilityFile, JsonArray.class);
                possibilities.put(possibilityFile.getName().toLowerCase(), jsonArray);
            } catch (Exception ex) {
                OneBlockModule.log("Failed to parse possibilities " + possibilityFile.getName() + ":");
                ex.printStackTrace();
            }
        }

        List<PhaseData> phaseDataList = new ArrayList<>();

        for (String phaseFileName : module.getSettings().phases) {
            File phaseFile = new File(module.getModuleFolder() + "/phases", phaseFileName);

            if (!phaseFile.exists()) {
                OneBlockModule.log("Failed find the phase file " + phaseFileName + "...");
                continue;
            }

            OneBlockModule.log("Checking " + phaseFileName);

            try {
                JsonObject jsonObject = JsonUtils.parseFile(phaseFile, JsonObject.class);
                PhaseData.fromJson(jsonObject, this, phaseFileName).ifPresent(phaseDataList::add);
            } catch (Exception ex) {
                OneBlockModule.log("Failed to parse phase " + phaseFile.getName() + ":");
                ex.printStackTrace();
            }
        }

        return phaseDataList.toArray(new PhaseData[0]);
    }

}
