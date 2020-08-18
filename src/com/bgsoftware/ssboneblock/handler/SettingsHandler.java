package com.bgsoftware.ssboneblock.handler;

import com.bgsoftware.ssboneblock.OneBlockPlugin;
import com.bgsoftware.ssboneblock.commands.commands.SSBCheckCmd;
import com.bgsoftware.ssboneblock.config.CommentedConfiguration;
import com.bgsoftware.ssboneblock.utils.BlockPosition;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    public final BlockPosition blockOffset;
    public final List<String> timerFormat;
    public final List<String> phases;

    public SettingsHandler(OneBlockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        cfg.syncWithConfig(file, plugin.getResource("config.yml"),  "config.yml");
        blockOffset = new BlockPosition(cfg.getString("block-offset"));
        timerFormat = Arrays.asList(ChatColor.translateAlternateColorCodes('&', cfg.getString("timer-format")).split("\n"));
        Collections.reverse(timerFormat);
        phases = cfg.getStringList("phases");

        if(cfg.getBoolean("inject-island-command", true))
            SuperiorSkyblockAPI.registerCommand(new SSBCheckCmd());
    }

}
