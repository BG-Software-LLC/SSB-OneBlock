package com.bgsoftware.ssboneblock.task;

import com.bgsoftware.ssboneblock.OneBlockModule;
import org.bukkit.scheduler.BukkitRunnable;

public final class SaveTimer extends BukkitRunnable {

    private static OneBlockModule plugin;
    private static SaveTimer timer = null;

    private SaveTimer() {
        timer = this;
        runTaskTimerAsynchronously(plugin.getJavaPlugin(), 6000L, 6000L);
    }

    @Override
    public void run() {
        plugin.getPhasesHandler().getDataStore().save();
    }

    public static void startTimer(OneBlockModule plugin) {
        SaveTimer.plugin = plugin;
        stopTimer();
        new SaveTimer();
    }

    public static void stopTimer() {
        if (timer != null)
            timer.cancel();
    }

}
