package com.bgsoftware.ssboneblock.handler;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.commands.commands.SSBCheckCmd;
import com.bgsoftware.ssboneblock.utils.BlockPosition;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    public final BlockPosition blockOffset;
    public final List<String> timerFormat;
    public final List<String> phases;

    public SettingsHandler(OneBlockModule plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists())
            plugin.saveResource("config.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), "config.yml");
        } catch (IOException error) {
            throw new RuntimeException(error);
        }

        blockOffset = new BlockPosition(cfg.getString("block-offset"));
        timerFormat = Arrays.asList(ChatColor.translateAlternateColorCodes('&', cfg.getString("timer-format")).split("\n"));
        Collections.reverse(timerFormat);
        phases = cfg.getStringList("phases");

        if (cfg.getBoolean("inject-island-command", true))
            SuperiorSkyblockAPI.registerCommand(new SSBCheckCmd());
    }

}
