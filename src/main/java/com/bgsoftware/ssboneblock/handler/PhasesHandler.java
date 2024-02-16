package com.bgsoftware.ssboneblock.handler;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.actions.Action;
import com.bgsoftware.ssboneblock.data.DataStore;
import com.bgsoftware.ssboneblock.lang.LocaleUtils;
import com.bgsoftware.ssboneblock.lang.Message;
import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.ssboneblock.phases.PhaseData;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

    private final OneBlockModule plugin;
    private final DataStore dataStore;
    private final PhaseData[] phaseData;

    public PhasesHandler(OneBlockModule plugin, DataStore dataStore) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        phaseData = loadData(plugin);
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

    public void runNextAction(Island island, Player player) {
        if (!canHaveOneBlock(island))
            return;

        IslandPhaseData islandPhaseData = this.dataStore.getPhaseData(island, true);

        if (islandPhaseData.getPhaseLevel() >= phaseData.length) {
            if (player != null)
                Message.NO_MORE_PHASES.send(player);
            return;
        }

        PhaseData phaseData = this.phaseData[islandPhaseData.getPhaseLevel()];
        Action action = phaseData.getAction(islandPhaseData.getPhaseBlock());

        Location oneBlockLocation = plugin.getSettings().blockOffset.applyToLocation(
                island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));

        if (action == null) {
            int nextPhaseLevel = islandPhaseData.getPhaseLevel() + 1 < this.phaseData.length ?
                    islandPhaseData.getPhaseLevel() + 1 : plugin.getSettings().phasesLoop ? 0 : -1;
            runNextActionTimer(island, player, oneBlockLocation, phaseData, nextPhaseLevel);
            return;
        }

        Optional.ofNullable(NextPhaseTimer.getTimer(island)).ifPresent(nextPhaseTimer -> {
            nextPhaseTimer.setRunFinishCallback(false);
            nextPhaseTimer.cancel();
        });

        action.run(oneBlockLocation, island, player);

        IslandPhaseData newPhaseData = this.dataStore.getPhaseData(island, false);

        if (newPhaseData == islandPhaseData)
            this.dataStore.setPhaseData(island, islandPhaseData.nextBlock());

        java.util.Locale locale = LocaleUtils.getLocale(player);
        Message.PHASE_PROGRESS.send(player, locale,
                islandPhaseData.getPhaseBlock() * 100 / phaseData.getActionsSize(),
                islandPhaseData.getPhaseBlock(),
                phaseData.getActionsSize());

        // We check for last phase here as well.
        if (plugin.getSettings().phasesLoop && islandPhaseData.getPhaseBlock() + 1 == phaseData.getActionsSize() &&
                islandPhaseData.getPhaseLevel() + 1 == this.phaseData.length)
            runNextActionTimer(island, player, oneBlockLocation, phaseData, 0);
    }

    private void runNextActionTimer(Island island, Player player, Location oneBlockLocation,
                                    PhaseData phaseData, int nextPhaseLevel) {
        if (NextPhaseTimer.getTimer(island) == null) {
            oneBlockLocation.getBlock().setType(Material.BEDROCK);
            if (nextPhaseLevel >= 0) {
                new NextPhaseTimer(island, phaseData.getNextPhaseCooldown(), () -> setPhaseLevel(island, nextPhaseLevel, player));
            }
        }
    }

    public boolean setPhaseLevel(Island island, int phaseLevel, Player player) {
        if (phaseLevel >= phaseData.length)
            return false;

        IslandPhaseData islandPhaseData = new IslandPhaseData(phaseLevel, 0);
        this.dataStore.setPhaseData(island, islandPhaseData);

        runNextAction(island, player);

        return true;
    }

    public boolean setPhaseBlock(Island island, int phaseBlock, Player player) {
        IslandPhaseData islandPhaseData = this.dataStore.getPhaseData(island, true);
        PhaseData phaseData = this.phaseData[islandPhaseData.getPhaseLevel()];

        if (phaseData.getAction(phaseBlock) == null)
            return false;

        this.dataStore.setPhaseData(island, new IslandPhaseData(islandPhaseData.getPhaseLevel(), phaseBlock));
        runNextAction(island, player);

        return true;
    }

    public boolean canHaveOneBlock(Island island) {
        return !island.isSpawn() && (plugin.getSettings().whitelistedSchematics.isEmpty() ||
                plugin.getSettings().whitelistedSchematics.contains(island.getSchematicName().toUpperCase()));
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private PhaseData[] loadData(OneBlockModule plugin) {
        File phasesFolder = new File(plugin.getModuleFolder(), "phases");
        File possibilitiesFolder = new File(plugin.getModuleFolder(), "possibilities");

        if (!phasesFolder.exists()) {
            phasesFolder.mkdirs();
            plugin.saveResource("phases/plains-phase.json");
            plugin.saveResource("phases/underground-phase.json");
            plugin.saveResource("phases/snow-phase.json");
            plugin.saveResource("phases/ocean-phase.json");
            plugin.saveResource("phases/jungle-phase.json");
            plugin.saveResource("phases/red-desert-phase.json");
            plugin.saveResource("phases/nether-phase.json");
            plugin.saveResource("phases/idyll-phase.json");
            plugin.saveResource("phases/desolate-phase.json");
            plugin.saveResource("phases/end-phase.json");
        }

        if (!possibilitiesFolder.exists()) {
            possibilitiesFolder.mkdirs();
            plugin.saveResource("possibilities/plains-blocks.json");
            plugin.saveResource("possibilities/plains-chests.json");
            plugin.saveResource("possibilities/plains-mobs.json");
            plugin.saveResource("possibilities/underground-blocks.json");
            plugin.saveResource("possibilities/underground-chests.json");
            plugin.saveResource("possibilities/underground-mobs.json");
            plugin.saveResource("possibilities/snow-blocks.json");
            plugin.saveResource("possibilities/snow-chests.json");
            plugin.saveResource("possibilities/snow-mobs.json");
            plugin.saveResource("possibilities/ocean-blocks.json");
            plugin.saveResource("possibilities/ocean-chests.json");
            plugin.saveResource("possibilities/ocean-mobs.json");
            plugin.saveResource("possibilities/jungle-blocks.json");
            plugin.saveResource("possibilities/jungle-chests.json");
            plugin.saveResource("possibilities/jungle-mobs.json");
            plugin.saveResource("possibilities/red-desert-blocks.json");
            plugin.saveResource("possibilities/red-desert-chests.json");
            plugin.saveResource("possibilities/red-desert-mobs.json");
            plugin.saveResource("possibilities/nether-blocks.json");
            plugin.saveResource("possibilities/nether-chests.json");
            plugin.saveResource("possibilities/nether-mobs.json");
            plugin.saveResource("possibilities/idyll-blocks.json");
            plugin.saveResource("possibilities/idyll-chests.json");
            plugin.saveResource("possibilities/idyll-mobs.json");
            plugin.saveResource("possibilities/desolate-blocks.json");
            plugin.saveResource("possibilities/desolate-chests.json");
            plugin.saveResource("possibilities/desolate-mobs.json");
            plugin.saveResource("possibilities/end-blocks.json");
            plugin.saveResource("possibilities/end-chests.json");
            plugin.saveResource("possibilities/end-mobs.json");
            plugin.saveResource("possibilities/superchest.json");
            plugin.saveResource("possibilities/rarechest.json");
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

        for (String phaseFileName : plugin.getSettings().phases) {
            File phaseFile = new File(plugin.getModuleFolder() + "/phases", phaseFileName);

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
