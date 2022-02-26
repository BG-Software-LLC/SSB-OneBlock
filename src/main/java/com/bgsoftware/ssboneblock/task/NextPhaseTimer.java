package com.bgsoftware.ssboneblock.task;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public final class NextPhaseTimer extends BukkitRunnable {

    private static final Map<Island, NextPhaseTimer> timers = new HashMap<>();
    private static final OneBlockModule plugin = OneBlockModule.getPlugin();

    private final Island island;
    private final ArmorStand[] armorStands = new ArmorStand[plugin.getSettings().timerFormat.size()];
    private final Runnable onFinish;
    private short time;

    public NextPhaseTimer(Island island, short time, Runnable onFinish){
        timers.put(island, this);

        this.island = island;
        this.time = time;
        this.onFinish = onFinish;

        int armorStandCounter = 0;

        for(String name : plugin.getSettings().timerFormat)
            armorStands[armorStandCounter++] = createArmorStand(LocationUtils.getOneBlock(island), armorStandCounter - 1, time, name);

        runTaskTimer(plugin.getJavaPlugin(), 20L, 20L);
    }

    @Override
    public void run() {
        if(time == 0){
            cancel();
            return;
        }

        int armorStandCounter = 0;

        time--;

        for(String name : plugin.getSettings().timerFormat)
            updateArmorStand(armorStands[armorStandCounter++], time, name);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        for (ArmorStand armorStand : armorStands)
            armorStand.remove();

        timers.remove(island);

        onFinish.run();

        super.cancel();
    }

    public static NextPhaseTimer getTimer(Island island){
        return timers.get(island);
    }

    public static void cancelTimers(){
        new HashMap<>(timers).values().forEach(BukkitRunnable::cancel);
    }

    private static void updateArmorStand(ArmorStand armorStand, int time, String name){
        armorStand.setCustomName(name.replace("{0}", time + ""));
    }

    private static ArmorStand createArmorStand(Location location, int yOffset, int time, String name){
        ArmorStand armorStand = location.getWorld().spawn(location.clone().add(0.5, 2 + (yOffset * 0.3), 0.5), ArmorStand.class);

        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.setVisible(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(name.replace("{0}", time + ""));

        return armorStand;
    }

}
