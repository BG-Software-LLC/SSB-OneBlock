package com.bgsoftware.ssboneblock;

import com.bgsoftware.ssboneblock.commands.CommandsHandler;
import com.bgsoftware.ssboneblock.handler.DataHandler;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.handler.SettingsHandler;
import com.bgsoftware.ssboneblock.listeners.BlocksListener;
import com.bgsoftware.ssboneblock.listeners.IslandsListener;
import com.bgsoftware.ssboneblock.nms.NMSAdapter;
import com.bgsoftware.ssboneblock.phases.PhasesContainer;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.task.SaveTimer;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneBlockModule extends PluginModule {

    private static OneBlockModule instance;

    private JavaPlugin javaPlugin;

    private final DataHandler dataHandler = new DataHandler(this);

    private PhasesHandler phasesHandler;
    private SettingsHandler settingsHandler;
    private NMSAdapter nmsAdapter;

    private boolean loadedData = false;

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

        CommandsHandler commandsHandler = new CommandsHandler(this);
        SimpleCommandMap commandMap = nmsAdapter.getCommandMap();
        commandMap.register("ssboneblock", commandsHandler);

        SaveTimer.startTimer(this);

        javaPlugin.getServer().getScheduler().runTaskLater(javaPlugin, () -> {
            if (!loadedData) {
                loadedData = true;
                dataHandler.loadDatabase();
            }
        }, 1L);
    }

    @Override
    public void onReload(SuperiorSkyblock plugin) {
        if (phasesHandler != null)
            dataHandler.saveDatabase();

        PhasesContainer phasesContainer = this.phasesHandler == null ? new PhasesContainer() :
                this.phasesHandler.getPhasesContainer();

        settingsHandler = new SettingsHandler(this);
        phasesHandler = new PhasesHandler(this, phasesContainer);

        Locale.reload();
    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        NextPhaseTimer.cancelTimers();
        SaveTimer.stopTimer();
        dataHandler.saveDatabase();
    }

    /**
     * @since 1.8.4.520
     */
    public void loadData(SuperiorSkyblock plugin) {
        loadedData = true;
        this.dataHandler.loadDatabase();
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
