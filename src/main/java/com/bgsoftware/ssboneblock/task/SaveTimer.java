package com.bgsoftware.ssboneblock.task;

import com.bgsoftware.ssboneblock.OneBlockPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class SaveTimer extends BukkitRunnable {

    private static OneBlockPlugin plugin;
    private static SaveTimer timer = null;

    private SaveTimer(){
        timer = this;
        runTaskTimerAsynchronously(plugin, 6000L, 6000L);
    }

    @Override
    public void run() {
        plugin.getDataHandler().saveDatabase();
    }

    public static void startTimer(OneBlockPlugin plugin){
        SaveTimer.plugin = plugin;
        stopTimer();
        new SaveTimer();
    }

    public static void stopTimer(){
        if(timer != null)
            timer.cancel();
    }

}
