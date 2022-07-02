package com.bgsoftware.ssboneblock.data;

import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataTypeContext;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

public final class SqlDataStore implements DataStore {

    private static final PersistentDataType<IslandPhaseData> PHASE_DATA_TYPE = new PersistentDataType<>(
            IslandPhaseData.class, PhasePersistentDataTypeContext.getInstance());
    private static final String PHASE_DATA_KEY = "oneblock:phase_data";

    @Override
    public IslandPhaseData getPhaseData(Island island, boolean createNew) {
        IslandPhaseData islandPhaseData = island.getPersistentDataContainer().get(PHASE_DATA_KEY, PHASE_DATA_TYPE);

        if (islandPhaseData != null || !createNew)
            return islandPhaseData;

        islandPhaseData = new IslandPhaseData(0, 0);
        setPhaseData(island, islandPhaseData);
        return islandPhaseData;
    }

    @Override
    public void setPhaseData(Island island, IslandPhaseData phaseData) {
        island.getPersistentDataContainer().put(PHASE_DATA_KEY, PHASE_DATA_TYPE, phaseData);
    }

    @Override
    public void setPhaseData(UUID islandUUID, IslandPhaseData phaseData) {
        Island island;

        SuperiorPlayer matchingPlayer = SuperiorSkyblockAPI.getPlayers().getSuperiorPlayer(islandUUID);
        if (matchingPlayer != null) {
            island = matchingPlayer.getIsland();
        } else {
            island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);
        }

        if (island != null)
            setPhaseData(island, phaseData);
    }

    @Override
    public void removeIsland(Island island) {
        // Do nothing - data is handled by SSB.
    }

    @Override
    public void load() {
        // Do nothing - data is handled by SSB.
    }

    @Override
    public void save() {
        // Do nothing - data is handled by SSB.
    }

    private static final class PhasePersistentDataTypeContext implements PersistentDataTypeContext<IslandPhaseData> {

        private static final PhasePersistentDataTypeContext INSTANCE = new PhasePersistentDataTypeContext();

        public static PhasePersistentDataTypeContext getInstance() {
            return INSTANCE;
        }

        @Override
        public byte[] serialize(IslandPhaseData islandPhaseData) {
            ByteArrayDataOutput data = ByteStreams.newDataOutput();
            data.writeInt(islandPhaseData.getPhaseLevel());
            data.writeInt(islandPhaseData.getPhaseBlock());
            return data.toByteArray();
        }

        @Override
        public IslandPhaseData deserialize(byte[] bytes) {
            ByteArrayDataInput data = ByteStreams.newDataInput(bytes);
            return new IslandPhaseData(data.readInt(), data.readInt());
        }

    }

}
