package com.bgsoftware.ssboneblock.top;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;

import java.util.Comparator;
import java.util.function.Function;

public class SortingTypes {

    public static final SortingType BY_BLOCKS = register("ONEBLOCK-BLOCKS",
            SortingComparators::getTotalBlocks, SortingComparators.ONEBLOCK_BLOCKS_COMPARATOR);
    public static final SortingType BY_LEVEL = register("ONEBLOCK-LEVEL",
            SortingComparators::getPhaseLevel, SortingComparators.ONEBLOCK_LEVEL_COMPARATOR);

    private SortingTypes() {
    }

    public static void registerSortingTypes() {
        // Do nothing, only trigger all the register calls.
    }

    private static SortingType register(String name, Function<Island, Number> valueFunction, Comparator<Island> comparator) {
        SortingType.register(name, valueFunction, comparator, false);
        return SortingType.getByName(name);
    }

}
