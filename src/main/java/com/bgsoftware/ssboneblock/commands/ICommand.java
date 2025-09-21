package com.bgsoftware.ssboneblock.commands;

import com.bgsoftware.ssboneblock.OneBlockModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ICommand {

    String getLabel();

    String getUsage(java.util.Locale locale);

    String getPermission();

    String getDescription(java.util.Locale locale);

    int getMinArgs();

    int getMaxArgs();

    void perform(OneBlockModule module, CommandSender sender, String[] args);

    List<String> tabComplete(OneBlockModule module, CommandSender sender, String[] args);

}
