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
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NextPhaseTimer extends BukkitRunnable {

    private static final Map<UUID, NextPhaseTimer> timers = new ConcurrentHashMap<>();
    private static final OneBlockModule plugin = OneBlockModule.getPlugin();

    private final List<Hologram> holograms = new LinkedList<>();
    private final Island island;
    private final Runnable onFinish;
    private short time;
    private boolean runFinishCallback = true;

    public NextPhaseTimer(Island island, short time, Runnable onFinish) {
        NextPhaseTimer oldTimer = timers.put(island.getUniqueId(), this);
        if (oldTimer != null)
            oldTimer.cancel();

        this.island = island;
        this.time = time;
        this.onFinish = onFinish;

        Location oneBlockLocation = plugin.getSettings().blockOffset.applyToLocation(
                island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));

        for (String name : plugin.getSettings().timerFormat) {
            Hologram hologram = createHologram(oneBlockLocation, this.holograms.size());
            if (hologram != null) {
                hologram.setHologramName(name.replace("{0}", time + ""));
                this.holograms.add(hologram);
            }
        }

        runTaskTimer(plugin.getJavaPlugin(), 20L, 20L);
    }

    public void setRunFinishCallback(boolean runFinishCallback) {
        this.runFinishCallback = runFinishCallback;
    }

    @Override
    public void run() {
        if (time == 0) {
            cancel();
            return;
        }

        int hologramCounter = 0;

        time--;

        Location oneBlockLocation = null;

        ListIterator<Hologram> iterator = this.holograms.listIterator();
        while (iterator.hasNext()) {
            Hologram hologram = iterator.next();

            if (!hologram.getHandle().isValid()) {
                if (oneBlockLocation == null) {
                    oneBlockLocation = plugin.getSettings().blockOffset.applyToLocation(
                            island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));
                }

                hologram = createHologram(oneBlockLocation, hologramCounter);
                iterator.set(hologram);
            }

            String name = plugin.getSettings().timerFormat.get(hologramCounter);
            hologram.setHologramName(name.replace("{0}", time + ""));

            ++hologramCounter;
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        for (Hologram hologram : holograms)
            hologram.removeHologram();

        timers.remove(island.getUniqueId());

        if (this.runFinishCallback)
            onFinish.run();

        super.cancel();
    }

    public static NextPhaseTimer getTimer(Island island) {
        return timers.get(island.getUniqueId());
    }

    public static void cancelTimers() {
        new HashMap<>(timers).values().forEach(BukkitRunnable::cancel);
    }

    private static Hologram createHologram(Location firstLocation, int index) {
        Location hologramLocation = firstLocation.clone().add(0.5, 2 + (index * 0.3), 0.5);
        return HologramFactory.createHologram(hologramLocation);
    }

}
