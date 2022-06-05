package com.bgsoftware.ssboneblock.phases;

import com.bgsoftware.superiorskyblock.api.island.Island;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class PhasesContainer {

    private final Map<Island, IslandPhaseData> islandPhaseData = new ConcurrentHashMap<>();

//    public int[] getPhaseStatus(Island island) {
//        IslandPhaseData islandPhaseData = this.islandPhaseData.get(island);
//        return islandPhaseData == null ? new int[]{0, 0} : new int[]{islandPhaseData.getPhaseLevel(), islandPhaseData.getPhaseBlock()};
//    }

    public void setPhaseData(Island island, IslandPhaseData islandPhaseData) {
        this.islandPhaseData.put(island, islandPhaseData);
    }

    public void forEach(BiConsumer<Island, IslandPhaseData> consumer) {
        islandPhaseData.forEach(consumer);
    }

    public IslandPhaseData getPhaseData(Island island, @Nullable Supplier<IslandPhaseData> islandPhaseDataSupplier) {
        return islandPhaseDataSupplier == null ? this.islandPhaseData.get(island) :
                this.islandPhaseData.computeIfAbsent(island, v -> islandPhaseDataSupplier.get());
    }

    public void removeIsland(Island island) {
        this.islandPhaseData.remove(island);
    }

}
