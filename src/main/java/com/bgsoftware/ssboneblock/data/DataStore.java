package com.bgsoftware.ssboneblock.data;

import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.superiorskyblock.api.island.Island;

public interface DataStore {

    IslandPhaseData getPhaseData(Island island, boolean createNew);

    void setPhaseData(Island island, IslandPhaseData phaseData);

    void removeIsland(Island island);

    void load();

    void save();

}
