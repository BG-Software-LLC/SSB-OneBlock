package com.bgsoftware.ssboneblock.task;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.factory.HologramFactory;
import com.bgsoftware.ssboneblock.utils.WorldUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FinishedPhasesHologram {

    private static final Map<UUID, List<Hologram>> holograms = new ConcurrentHashMap<>();
    private static final OneBlockModule module = OneBlockModule.getModule();

    private FinishedPhasesHologram() {

    }

    public static void show(Island island) {
        List<String> hologramLines = module.getSettings().finishedPhasesHologram;
        if (hologramLines.isEmpty())
            return;

        remove(island);

        Location oneBlockLocation = WorldUtils.getOneBlock(island);
        List<Hologram> islandHolograms = new LinkedList<>();

        for (int i = 0; i < hologramLines.size(); i++) {
            Hologram hologram = createHologram(oneBlockLocation, i);
            if (hologram == null)
                continue;

            hologram.setHologramName(hologramLines.get(i));
            islandHolograms.add(hologram);
        }

        if (!islandHolograms.isEmpty())
            holograms.put(island.getUniqueId(), islandHolograms);
    }

    public static void remove(Island island) {
        List<Hologram> islandHolograms = holograms.remove(island.getUniqueId());
        if (islandHolograms == null)
            return;

        islandHolograms.forEach(Hologram::removeHologram);
    }

    public static void removeAll() {
        new HashMap<>(holograms).values().forEach(islandHolograms ->
                islandHolograms.forEach(Hologram::removeHologram));
        holograms.clear();
    }

    private static Hologram createHologram(Location firstLocation, int index) {
        Location hologramLocation = firstLocation.clone().add(0.5, 2 + (index * 0.3), 0.5);
        return HologramFactory.createHologram(hologramLocation);
    }

}
