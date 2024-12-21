package com.bgsoftware.ssboneblock;

import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSHandlersFactory;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.ssboneblock.commands.CommandsHandler;
import com.bgsoftware.ssboneblock.data.DataType;
import com.bgsoftware.ssboneblock.data.FlatDataStore;
import com.bgsoftware.ssboneblock.data.SqlDataStore;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.handler.SettingsHandler;
import com.bgsoftware.ssboneblock.lang.Message;
import com.bgsoftware.ssboneblock.listeners.BlocksListener;
import com.bgsoftware.ssboneblock.listeners.IslandsListener;
import com.bgsoftware.ssboneblock.nms.ModuleNMSConfiguration;
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
        this.plugin = plugin;

        if (!loadNMSAdapter()) {
            throw new RuntimeException("Couldn't find a valid nms support for your version.");
        }

        onReload(plugin);

        String label;
        if (nmsAdapter.getCommandMap().getCommand("oneblock") == null) {
            label = "oneblock";
        } else {
            label = "ssboneblock";
            getLogger().warning("The command '/oneblock' is already registered, defaulting to '/ssboneblock' instead.");
        }

        CommandsHandler commandsHandler = new CommandsHandler(this, label);
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
        if (this.phasesHandler != null)
            this.phasesHandler.getDataStore().save();

        this.settingsHandler = new SettingsHandler(this);
        this.phasesHandler = new PhasesHandler(this, this.phasesHandler != null ? this.phasesHandler.getDataStore() :
                this.settingsHandler.dataType == DataType.FLAT ? new FlatDataStore(this) : new SqlDataStore());

        Message.reload();
    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        NextPhaseTimer.cancelTimers();
        SaveTimer.stopTimer();
        if (this.phasesHandler != null)
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
        try {
            INMSLoader nmsLoader = NMSHandlersFactory.createNMSLoader((JavaPlugin) this.plugin,
                    new ModuleNMSConfiguration(this), getClassLoader());

            this.nmsAdapter = nmsLoader.loadNMSHandler(NMSAdapter.class);

            return true;
        } catch (NMSLoadException error) {
            log("&cThe plugin doesn't support your minecraft version.");
            log("&cPlease try a different version.");

            error.printStackTrace();
        }

        return false;

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

    public SuperiorSkyblock getPlugin() {
        return plugin;
    }

    public static void log(String message) {
        instance.getLogger().info(message);
    }

    public static OneBlockModule getModule() {
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
