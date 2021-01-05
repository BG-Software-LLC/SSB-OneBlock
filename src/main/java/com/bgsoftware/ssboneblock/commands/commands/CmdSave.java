package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.OneBlockPlugin;
import com.bgsoftware.ssboneblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class CmdSave implements ICommand {

    @Override
    public String getLabel() {
        return "save";
    }

    @Override
    public String getUsage() {
        return "oneblock save";
    }

    @Override
    public String getPermission() {
        return "oneblock.save";
    }

    @Override
    public String getDescription() {
        return "Save all data to disk.";
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
           sender.sendMessage(ChatColor.YELLOW + "Successfully saved all data (Took " + (System.currentTimeMillis() - startTime) + "ms)!");
        });
    }

    @Override
    public List<String> tabComplete(OneBlockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
