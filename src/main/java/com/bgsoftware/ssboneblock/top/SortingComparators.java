package com.bgsoftware.ssboneblock.top;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.superiorskyblock.api.island.Island;

import java.util.Comparator;

public class SortingComparators {

    private static final PhasesHandler phasesHandler = OneBlockModule.getModule().getPhasesHandler();

    private static final Comparator<Island> ISLAND_NAMES_COMPARATOR = (island1, island2) -> {
        String firstName = island1.getStrippedName().isEmpty() ? island1.getOwner().getName() : island1.getStrippedName();
        String secondName = island2.getStrippedName().isEmpty() ? island2.getOwner().getName() : island2.getStrippedName();
        return firstName.compareTo(secondName);
    };
    public static final Comparator<Island> ONEBLOCK_BLOCKS_COMPARATOR = (island1, island2) -> {
        int compare = Integer.compare(getTotalBlocks(island2), getTotalBlocks(island1));
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(island1, island2) : compare;
    };
    public static final Comparator<Island> ONEBLOCK_LEVEL_COMPARATOR = (island1, island2) -> {
        int compare = Integer.compare(getPhaseLevel(island2), getPhaseLevel(island1));
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(island1, island2) : compare;
    };

    private SortingComparators() {
    }

    public static int getPhaseLevel(Island island) {
        IslandPhaseData islandPhaseData = phasesHandler.getDataStore().getPhaseData(island, false);

        if (islandPhaseData == null) {
            return 1;
        }

        return islandPhaseData.getPhaseLevel() + 1;
    }

    public static int getTotalBlocks(Island island) {
        IslandPhaseData islandPhaseData = phasesHandler.getDataStore().getPhaseData(island, false);

        if (islandPhaseData == null) {
            return 0;
        }

        return phasesHandler.getTotalBlocks(islandPhaseData);
    }

}
