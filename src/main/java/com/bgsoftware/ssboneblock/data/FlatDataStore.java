package com.bgsoftware.ssboneblock.data;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FlatDataStore implements DataStore {

    private final Map<Island, IslandPhaseData> islandPhaseData = new ConcurrentHashMap<>();

    private final OneBlockModule module;

    public FlatDataStore(OneBlockModule module) {
        this.module = module;
    }

    @Override
    public IslandPhaseData getPhaseData(Island island, boolean createNew) {
        return !createNew ? this.islandPhaseData.get(island) :
                this.islandPhaseData.computeIfAbsent(island, v -> new IslandPhaseData(0, 0));
    }

    @Override
    public void setPhaseData(Island island, IslandPhaseData phaseData) {
        this.islandPhaseData.put(island, phaseData);
    }

    @Override
    public void removeIsland(Island island) {
        this.islandPhaseData.remove(island);
    }

    @Override
    public void load() {
        File file = new File(module.getDataStoreFolder(), "database.json");

        convertOldDatabase(file);

        if (file.isDirectory())
            file.delete();

        if (!file.exists())
            return;

        try (FileReader reader = new FileReader(file)) {
            JsonArray jsonArray = JsonUtils.getGson().fromJson(reader, JsonArray.class);
            if (jsonArray != null) {
                for (JsonElement islandDataElement : jsonArray) {
                    try {
                        JsonObject islandData = (JsonObject) islandDataElement;
                        Island island = SuperiorSkyblockAPI.getPlayer(UUID.fromString(islandData.get("island").getAsString())).getIsland();
                        int phaseLevel = islandData.get("phase-level").getAsInt();
                        int phaseBlock = islandData.get("phase-block").getAsInt();
                        setPhaseData(island, new IslandPhaseData(phaseLevel, phaseBlock));
                    } catch (Throwable error) {
                        OneBlockModule.log("Failed to parse data for element: " + islandDataElement);
                        error.printStackTrace();
                    }
                }
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }

    @Override
    public void save() {
        JsonArray islandData = new JsonArray();

        for (Island island : SuperiorSkyblockAPI.getGrid().getIslands()) {
            IslandPhaseData islandPhaseData = module.getPhasesHandler().getDataStore().getPhaseData(island, false);
            if (islandPhaseData != null && (islandPhaseData.getPhaseBlock() > 0 || islandPhaseData.getPhaseLevel() > 0)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("island", island.getOwner().getUniqueId() + "");
                jsonObject.addProperty("phase-level", islandPhaseData.getPhaseLevel());
                jsonObject.addProperty("phase-block", islandPhaseData.getPhaseBlock());
                islandData.add(jsonObject);
            }
        }

        File file = new File(module.getDataStoreFolder(), "database.json");

        if (file.isDirectory())
            file.delete();

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(JsonUtils.getGson().toJson(islandData));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void convertOldDatabase(File newFile) {
        File oldFile = new File(module.getModuleFolder(), "database.json");
        if (oldFile.exists()) {
            newFile.getParentFile().mkdirs();
            oldFile.renameTo(newFile);
        }
    }

}
