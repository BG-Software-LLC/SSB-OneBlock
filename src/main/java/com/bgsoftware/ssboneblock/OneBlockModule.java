package com.bgsoftware.ssboneblock;

import com.bgsoftware.common.mappings.MappingsChecker;
import com.bgsoftware.common.remaps.TestRemaps;
import com.bgsoftware.ssboneblock.commands.CommandsHandler;
import com.bgsoftware.ssboneblock.data.DataType;
import com.bgsoftware.ssboneblock.data.FlatDataStore;
import com.bgsoftware.ssboneblock.data.SqlDataStore;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.handler.SettingsHandler;
import com.bgsoftware.ssboneblock.lang.Message;
import com.bgsoftware.ssboneblock.listeners.BlocksListener;
import com.bgsoftware.ssboneblock.listeners.IslandsListener;
import com.bgsoftware.ssboneblock.nms.NMSAdapter;
import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.ssboneblock.phases.PhaseData;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.task.SaveTimer;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class OneBlockModule extends PluginModule {

    private static OneBlockModule instance;

    private SuperiorSkyblock plugin;

    private PhasesHandler phasesHandler;
    private SettingsHandler settingsHandler;
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

        this.plugin = plugin;

        onReload(plugin);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        SimpleCommandMap commandMap = nmsAdapter.getCommandMap();
        commandMap.register("ssboneblock", commandsHandler);

        try {
            registerPlaceholders();
        } catch (Throwable ignored) {
            // API methods doesn't exist yet.
        }

        SaveTimer.startTimer(this);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> phasesHandler.getDataStore().load(), 1L);
    }

    @Override
    public void onReload(SuperiorSkyblock plugin) {
        if (phasesHandler != null)
            phasesHandler.getDataStore().save();

        settingsHandler = new SettingsHandler(this);
        phasesHandler = new PhasesHandler(this, phasesHandler != null ? phasesHandler.getDataStore() :
                settingsHandler.dataType == DataType.FLAT ? new FlatDataStore(this) : new SqlDataStore());

        Message.reload();
    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        NextPhaseTimer.cancelTimers();
        SaveTimer.stopTimer();
        this.phasesHandler.getDataStore().save();
    }

    @Override
    public void loadData(SuperiorSkyblock plugin) {
        this.phasesHandler.getDataStore().load();
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

            String mappingVersionHash = nmsAdapter.getMappingsHash();


            if (mappingVersionHash != null && !MappingsChecker.checkMappings(mappingVersionHash, version)) {
                log("Error while loading adapter - your version mappings are not supported... Please contact @Ome_R");
                log("Your mappings version: " + mappingVersionHash);
                return false;
            }

        } catch (Exception ex) {
            log("Invalid adapter for version " + version);
            return false;
        }

        File mappingsFile = new File("mappings");
        if (mappingsFile.exists()) {
            try {
                TestRemaps.testRemapsForClasses(mappingsFile, Class.forName("com.bgsoftware.ssboneblock.nms.NMSAdapter_" + version));
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        return true;
    }

    public PhasesHandler getPhasesHandler() {
        return phasesHandler;
    }

    public SettingsHandler getSettings() {
        return settingsHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public JavaPlugin getJavaPlugin() {
        return (JavaPlugin) plugin;
    }

    public static void log(String message) {
        instance.getLogger().info(message);
    }

    public static OneBlockModule getPlugin() {
        return instance;
    }

    private void registerPlaceholders() {
        RegisteredServiceProvider<PlaceholdersService> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(PlaceholdersService.class);

        if (registeredServiceProvider == null)
            return;

        PlaceholdersService placeholdersService = registeredServiceProvider.getProvider();

        if (placeholdersService == null)
            return;

        placeholdersService.registerPlaceholder("oneblock_phase_level", (island, superiorPlayer) -> {
            if (island == null)
                return null;

            IslandPhaseData islandPhaseData = phasesHandler.getDataStore().getPhaseData(island, false);

            if (islandPhaseData == null)
                return "0";

            return String.valueOf(islandPhaseData.getPhaseLevel() + 1);
        });

        placeholdersService.registerPlaceholder("oneblock_phase_block", (island, superiorPlayer) -> {
            if (island == null)
                return null;

            IslandPhaseData islandPhaseData = phasesHandler.getDataStore().getPhaseData(island, false);

            if (islandPhaseData == null)
                return "0";

            return String.valueOf(islandPhaseData.getPhaseBlock());
        });

        placeholdersService.registerPlaceholder("oneblock_progress", (island, superiorPlayer) -> {
            if (island == null)
                return null;

            IslandPhaseData islandPhaseData = phasesHandler.getDataStore().getPhaseData(island, false);

            if (islandPhaseData == null)
                return "0";

            PhaseData phaseData = phasesHandler.getPhaseData(islandPhaseData);

            if (phaseData == null)
                return "0";

            return String.valueOf(islandPhaseData.getPhaseBlock() * 100 / phaseData.getActionsSize());
        });

        placeholdersService.registerPlaceholder("oneblock_blocks_in_phase", (island, superiorPlayer) -> {
            if (island == null)
                return null;

            IslandPhaseData islandPhaseData = phasesHandler.getDataStore().getPhaseData(island, false);

            if (islandPhaseData == null)
                return "0";

            PhaseData phaseData = phasesHandler.getPhaseData(islandPhaseData);

            if (phaseData == null)
                return "0";

            return String.valueOf(phaseData.getActionsSize());
        });

        placeholdersService.registerPlaceholder("oneblock_phase_name", (island, superiorPlayer) -> {
            if (island == null)
                return null;

            IslandPhaseData islandPhaseData = phasesHandler.getDataStore().getPhaseData(island, false);

            if (islandPhaseData == null)
                return "";

            PhaseData phaseData = phasesHandler.getPhaseData(islandPhaseData);

            if (phaseData == null)
                return "";

            return phaseData.getName();
        });

        placeholdersService.registerPlaceholder("oneblock_next_phase_name", (island, superiorPlayer) -> {
            if (island == null)
                return null;

            IslandPhaseData islandPhaseData = phasesHandler.getDataStore().getPhaseData(island, false);

            if (islandPhaseData == null)
                return "";

            PhaseData phaseData = phasesHandler.getPhaseData(islandPhaseData.getPhaseLevel() + 1);

            if (phaseData == null)
                return "";

            return phaseData.getName();
        });

    }

}
