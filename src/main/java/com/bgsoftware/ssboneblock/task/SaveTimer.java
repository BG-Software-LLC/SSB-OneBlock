package com.bgsoftware.ssboneblock.task;

import com.bgsoftware.ssboneblock.OneBlockModule;
import org.bukkit.scheduler.BukkitRunnable;

public final class SaveTimer extends BukkitRunnable {

    private static OneBlockModule module;
    private static SaveTimer timer = null;

    private SaveTimer() {
        timer = this;
        runTaskTimerAsynchronously(module.getPlugin(), 6000L, 6000L);
    }

    @Override
    public void run() {
        module.getPhasesHandler().getDataStore().save();
    }

    public static void startTimer(OneBlockModule module) {
        SaveTimer.module = module;
        stopTimer();
        new SaveTimer();
    }

    public static void stopTimer() {
        if (timer != null)
            timer.cancel();
    }

}
