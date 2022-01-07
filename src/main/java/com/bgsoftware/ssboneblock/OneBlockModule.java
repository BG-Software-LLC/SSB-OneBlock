package com.bgsoftware.ssboneblock;

import com.bgsoftware.ssboneblock.commands.CommandsHandler;
import com.bgsoftware.ssboneblock.handler.DataHandler;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.handler.SettingsHandler;
import com.bgsoftware.ssboneblock.listeners.BlocksListener;
import com.bgsoftware.ssboneblock.listeners.IslandsListener;
import com.bgsoftware.ssboneblock.nms.NMSAdapter;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.task.SaveTimer;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneBlockModule extends PluginModule {

    private static OneBlockModule instance;

    private JavaPlugin javaPlugin;

    private PhasesHandler phasesHandler;
    private SettingsHandler settingsHandler;
    private DataHandler dataHandler;
    private NMSAdapter nmsAdapter;

    public OneBlockModule() {
        super("OneBlock", "Ome_R");
        instance = this;
    }

    @Override
    public void onEnable(SuperiorSkyblock plugin) {
        if (!loadNMSAdapter()) {
            throw new RuntimeException("Couldn't find a valid nms support for your version.");
        }

        javaPlugin = (JavaPlugin) plugin;

        onReload(plugin);
        dataHandler = new DataHandler(this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        javaPlugin.getCommand("oneblock").setExecutor(commandsHandler);
        javaPlugin.getCommand("oneblock").setTabCompleter(commandsHandler);

        SaveTimer.startTimer(this);
    }

    @Override
    public void onReload(SuperiorSkyblock plugin) {
        settingsHandler = new SettingsHandler(this);
        phasesHandler = new PhasesHandler(this);
        Locale.reload();
    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        NextPhaseTimer.cancelTimers();
        SaveTimer.stopTimer();
        dataHandler.saveDatabase();
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblock superiorSkyblock) {
        return new Listener[]{new IslandsListener(this), new BlocksListener(this)};
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock superiorSkyblock) {
        return null;
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock superiorSkyblock) {
        return null;
    }

    private boolean loadNMSAdapter() {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAdapter = (NMSAdapter) Class.forName("com.bgsoftware.ssboneblock.nms.NMSAdapter_" + version).newInstance();
            return true;
        } catch (Exception ex) {
            log("Invalid adapter for version " + version);
            return false;
        }
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

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public static void log(String message) {
        instance.getLogger().info(message);
    }

    public static OneBlockModule getPlugin() {
        return instance;
    }

}
