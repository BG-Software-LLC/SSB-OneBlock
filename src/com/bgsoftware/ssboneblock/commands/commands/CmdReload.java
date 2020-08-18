package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.OneBlockPlugin;
import com.bgsoftware.ssboneblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class CmdReload implements ICommand {

    @Override
    public String getLabel() {
        return "reload";
    }

    @Override
    public String getUsage() {
        return "oneblock reload";
    }

    @Override
    public String getPermission() {
        return "oneblock.reload";
    }

    @Override
    public String getDescription() {
        return "Reload all settings of the plugin.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public void perform(OneBlockPlugin plugin, CommandSender sender, String[] args) {
        long startTime = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDataHandler().saveDatabase();
            plugin.reloadPlugin();
            sender.sendMessage(ChatColor.YELLOW + "Successfully loaded all files (Took " + (System.currentTimeMillis() - startTime) + "ms)!");
        });
    }

    @Override
    public List<String> tabComplete(OneBlockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
