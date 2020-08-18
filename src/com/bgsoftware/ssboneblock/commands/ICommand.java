package com.bgsoftware.ssboneblock.commands;

import com.bgsoftware.ssboneblock.OneBlockPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ICommand {

    String getLabel();

    String getUsage();

    String getPermission();

    String getDescription();

    int getMinArgs();

    int getMaxArgs();

    void perform(OneBlockPlugin plugin, CommandSender sender, String[] args);

    List<String> tabComplete(OneBlockPlugin plugin, CommandSender sender, String[] args);

}
