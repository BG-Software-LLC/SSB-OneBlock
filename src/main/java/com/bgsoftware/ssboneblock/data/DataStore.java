package com.bgsoftware.ssboneblock.data;

import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.superiorskyblock.api.island.Island;

import java.util.UUID;

public interface DataStore {

    IslandPhaseData getPhaseData(Island island, boolean createNew);

    void setPhaseData(Island island, IslandPhaseData phaseData);

    void setPhaseData(UUID islandUUID, IslandPhaseData phaseData);

    void removeIsland(Island island);

    void load();

    void save();

}
