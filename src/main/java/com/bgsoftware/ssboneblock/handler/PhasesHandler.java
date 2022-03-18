package com.bgsoftware.ssboneblock.handler;

import com.bgsoftware.ssboneblock.Locale;
import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.actions.Action;
import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.ssboneblock.phases.PhaseData;
import com.bgsoftware.ssboneblock.phases.PhasesContainer;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.ssboneblock.utils.LocaleUtils;
import com.bgsoftware.ssboneblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PhasesHandler {

    private static final Supplier<IslandPhaseData> NEW_ISLAND_PHASE_DATA = () -> new IslandPhaseData(0, 0);

    private final Map<String, JsonArray> possibilities = new ConcurrentHashMap<>();

    private final OneBlockModule plugin;
    private final PhasesContainer phasesContainer;
    private final PhaseData[] phaseData;

    public PhasesHandler(OneBlockModule plugin, PhasesContainer phasesContainer) {
        this.plugin = plugin;
        this.phasesContainer = phasesContainer;
        phaseData = loadData(plugin);
    }

    public PhasesContainer getPhasesContainer() {
        return phasesContainer;
    }

    public JsonArray getPossibilities(String possibilities) {
        return this.possibilities.getOrDefault(possibilities.toLowerCase(), new JsonArray());
    }

    public void runNextAction(Island island, Player player) {
        if (!canHaveOneBlock(island))
            return;

        IslandPhaseData islandPhaseData = this.phasesContainer.getPhaseData(island, NEW_ISLAND_PHASE_DATA);

        if (islandPhaseData.getPhaseLevel() >= phaseData.length) {
            if (player != null)
                Locale.NO_MORE_PHASES.send(player);
            return;
        }

        PhaseData phaseData = this.phaseData[islandPhaseData.getPhaseLevel()];
        Action action = phaseData.getAction(islandPhaseData.getPhaseBlock());

        if (action == null) {
            if (NextPhaseTimer.getTimer(island) == null) {
                LocationUtils.getOneBlock(island).getBlock().setType(Material.BEDROCK);
                if (islandPhaseData.getPhaseLevel() + 1 < this.phaseData.length)
                    new NextPhaseTimer(island, phaseData.getNextPhaseCooldown(),
                            () -> setPhaseLevel(island, islandPhaseData.getPhaseLevel() + 1, player));
            }
            return;
        }

        action.run(LocationUtils.getOneBlock(island), island, player);
        islandPhaseData.setPhaseBlock(islandPhaseData.getPhaseBlock() + 1);

        java.util.Locale locale = LocaleUtils.getLocale(player);
        if (player != null && !Locale.PHASE_PROGRESS.isEmpty(locale))
            plugin.getNMSAdapter().sendActionBar(player, Locale.PHASE_PROGRESS
                    .getMessage(locale, islandPhaseData.getPhaseBlock() * 100 / phaseData.getActionsSize()));
    }

    public boolean setPhaseLevel(Island island, int phaseLevel, Player player) {
        if (phaseLevel >= phaseData.length)
            return false;

        IslandPhaseData islandPhaseData = this.phasesContainer.getPhaseData(island, NEW_ISLAND_PHASE_DATA);

        islandPhaseData.setPhaseLevel(phaseLevel);
        islandPhaseData.setPhaseBlock(0);
        runNextAction(island, player);

        return true;
    }

    public boolean setPhaseBlock(Island island, int phaseBlock, Player player) {
        IslandPhaseData islandPhaseData = this.phasesContainer.getPhaseData(island, NEW_ISLAND_PHASE_DATA);
        PhaseData phaseData = this.phaseData[islandPhaseData.getPhaseLevel()];

        if (phaseData.getAction(phaseBlock) == null)
            return false;

        islandPhaseData.setPhaseBlock(phaseBlock);
        runNextAction(island, player);

        return true;
    }

    public void loadIslandData(JsonObject jsonObject) {
        Preconditions.checkArgument(jsonObject.has("island"), "Data is missing key \"island\"!");
        Preconditions.checkArgument(jsonObject.has("phase-level"), "Data is missing key \"phase-level\"!");
        Preconditions.checkArgument(jsonObject.has("phase-block"), "Data is missing key \"phase-block\"!");

        Island island = SuperiorSkyblockAPI.getPlayer(UUID.fromString(jsonObject.get("island").getAsString())).getIsland();
        int phaseLevel = jsonObject.get("phase-level").getAsInt();
        int phaseBlock = jsonObject.get("phase-block").getAsInt();

        this.phasesContainer.setPhaseData(island, new IslandPhaseData(phaseLevel, phaseBlock));
    }

    public JsonArray saveIslandData() {
        JsonArray islandData = new JsonArray();

        this.phasesContainer.forEach((island, islandPhaseData) -> {
            if (islandPhaseData.getPhaseBlock() > 0 || islandPhaseData.getPhaseLevel() > 0) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("island", island.getOwner().getUniqueId() + "");
                jsonObject.addProperty("phase-level", islandPhaseData.getPhaseLevel());
                jsonObject.addProperty("phase-block", islandPhaseData.getPhaseBlock());
                islandData.add(jsonObject);
            }
        });

        return islandData;
    }

    public boolean canHaveOneBlock(Island island) {
        return !island.isSpawn() && (plugin.getSettings().whitelistedSchematics.isEmpty() ||
                plugin.getSettings().whitelistedSchematics.contains(island.getSchematicName().toUpperCase()));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private PhaseData[] loadData(OneBlockModule plugin) {
        File phasesFolder = new File(plugin.getDataFolder(), "phases");
        File possibilitiesFolder = new File(plugin.getDataFolder(), "possibilities");

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
                JsonArray jsonArray = JsonUtils.getGson().fromJson(new FileReader(possibilityFile), JsonArray.class);
                possibilities.put(possibilityFile.getName().toLowerCase(), jsonArray);
            } catch (Exception ex) {
                OneBlockModule.log("Failed to parse possibilities " + possibilityFile.getName() + ":");
                ex.printStackTrace();
            }
        }

        List<PhaseData> phaseDataList = new ArrayList<>();

        for (String phaseFileName : plugin.getSettings().phases) {
            File phaseFile = new File(plugin.getDataFolder() + "/phases", phaseFileName);

            if (!phaseFile.exists()) {
                OneBlockModule.log("Failed find the phase file " + phaseFileName + "...");
                continue;
            }

            OneBlockModule.log("Checking " + phaseFileName);

            try {
                JsonObject jsonObject = JsonUtils.getGson().fromJson(new FileReader(phaseFile), JsonObject.class);
                phaseDataList.add(PhaseData.fromJson(jsonObject, this));
            } catch (Exception ex) {
                OneBlockModule.log("Failed to parse phase " + phaseFile.getName() + ":");
                ex.printStackTrace();
            }
        }

        return phaseDataList.toArray(new PhaseData[0]);
    }

}
