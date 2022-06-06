package com.bgsoftware.ssboneblock.task;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.factory.HologramFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NextPhaseTimer extends BukkitRunnable {

    private static final Map<UUID, NextPhaseTimer> timers = new HashMap<>();
    private static final OneBlockModule plugin = OneBlockModule.getPlugin();

    private final List<Hologram> holograms = new LinkedList<>();
    private final Island island;
    private final Runnable onFinish;
    private short time;

    public NextPhaseTimer(Island island, short time, Runnable onFinish) {
        timers.put(island.getUniqueId(), this);

        this.island = island;
        this.time = time;
        this.onFinish = onFinish;

        int hologramCounter = 0;

        Location oneBlockLocation = plugin.getSettings().blockOffset.applyToLocation(
                island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));

        for (String name : plugin.getSettings().timerFormat) {
            Location hologramLocation = oneBlockLocation.clone().add(0.5, 2 + ((hologramCounter - 1) * 0.3), 0.5);
            Hologram hologram = HologramFactory.createHologram(hologramLocation);
            if (hologram != null) {
                hologram.setHologramName(name.replace("{0}", time + ""));
                holograms.add(hologram);
            }
        }

        runTaskTimer(plugin.getJavaPlugin(), 20L, 20L);
    }

    @Override
    public void run() {
        if (time == 0) {
            cancel();
            return;
        }

        int hologramCounter = 0;

        time--;

        for (Hologram hologram : holograms) {
            String name = plugin.getSettings().timerFormat.get(hologramCounter++);
            hologram.setHologramName(name.replace("{0}", time + ""));
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        for (Hologram hologram : holograms)
            hologram.removeHologram();

        timers.remove(island.getUniqueId());

        onFinish.run();

        super.cancel();
    }

    public static NextPhaseTimer getTimer(Island island) {
        return timers.get(island.getUniqueId());
    }

    public static void cancelTimers() {
        new HashMap<>(timers).values().forEach(BukkitRunnable::cancel);
    }

}
