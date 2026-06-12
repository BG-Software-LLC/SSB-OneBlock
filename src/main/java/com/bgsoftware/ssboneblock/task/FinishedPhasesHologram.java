package com.bgsoftware.ssboneblock.task;

import com.bgsoftware.ssboneblock.factory.HologramFactory;
import com.bgsoftware.ssboneblock.utils.WorldUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FinishedPhasesHologram {

    private static final List<String> HOLOGRAM_LINES = Collections.unmodifiableList(Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&e&lUkończono wszystkie fazy!"),
            ChatColor.translateAlternateColorCodes('&', "&7Użyj &b/fazy &7aby zmienić fazę wyspy")
    ));

    private static final Map<UUID, List<Hologram>> holograms = new ConcurrentHashMap<>();

    private FinishedPhasesHologram() {

    }

    public static void show(Island island) {
        remove(island);

        Location oneBlockLocation = WorldUtils.getOneBlock(island);
        List<Hologram> islandHolograms = new LinkedList<>();

        for (int i = 0; i < HOLOGRAM_LINES.size(); i++) {
            Hologram hologram = createHologram(oneBlockLocation, i);
            if (hologram == null)
                continue;

            hologram.setHologramName(HOLOGRAM_LINES.get(i));
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
