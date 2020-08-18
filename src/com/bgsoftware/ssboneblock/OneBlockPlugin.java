package com.bgsoftware.ssboneblock;

import com.bgsoftware.ssboneblock.commands.CommandsHandler;
import com.bgsoftware.ssboneblock.handler.DataHandler;
import com.bgsoftware.ssboneblock.handler.SettingsHandler;
import com.bgsoftware.ssboneblock.listeners.BlocksListener;
import com.bgsoftware.ssboneblock.listeners.IslandsListener;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.nms.NMSAdapter;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.task.SaveTimer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneBlockPlugin extends JavaPlugin {

    private static OneBlockPlugin plugin;

    private PhasesHandler phasesHandler;
    private SettingsHandler settingsHandler;
    private DataHandler dataHandler;
    private NMSAdapter nmsAdapter;
    private boolean shouldEnable = false;
    private boolean legacy = false;

    @Override
    public void onLoad() {
        shouldEnable = loadNMSAdapter();
        legacy = nmsAdapter.isLegacy();
        if(!shouldEnable)
            Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().disablePlugin(this));
    }

    @Override
    public void onEnable() {
        if(!shouldEnable)
            return;

        plugin = this;

        reloadPlugin();
        dataHandler = new DataHandler(this);

        getServer().getPluginManager().registerEvents(new IslandsListener(this), this);
        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("oneblock").setExecutor(commandsHandler);
        getCommand("oneblock").setTabCompleter(commandsHandler);

        SaveTimer.startTimer(this);
    }

    @Override
    public void onDisable() {
        NextPhaseTimer.cancelTimers();
        SaveTimer.stopTimer();
        dataHandler.saveDatabase();
    }

    private boolean loadNMSAdapter(){
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAdapter = (NMSAdapter) Class.forName("com.bgsoftware.ssboneblock.nms.NMSAdapter_" + version).newInstance();
            return true;
        }catch(Exception ex){
            log("Invalid adapter for version " + version);
            return false;
        }
    }

    public void reloadPlugin(){
        settingsHandler = new SettingsHandler(this);
        phasesHandler = new PhasesHandler(this);
        Locale.reload();
    }

    public PhasesHandler getPhasesHandler() {
        return phasesHandler;
    }

    public SettingsHandler getSettings() {
        return settingsHandler;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public boolean isLegacy() {
        return legacy;
    }

    public static void log(String message){
        plugin.getLogger().info(message);
    }

    public static OneBlockPlugin getPlugin() {
        return plugin;
    }

}
