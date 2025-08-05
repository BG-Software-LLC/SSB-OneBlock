package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.commands.ICommand;
import com.bgsoftware.ssboneblock.lang.Message;
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
    public String getUsage(java.util.Locale locale) {
        return "save";
    }

    @Override
    public String getPermission() {
        return "oneblock.save";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SAVE.getMessage(locale);
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
    public void perform(OneBlockModule module, CommandSender sender, String[] args) {
        long startTime = System.currentTimeMillis();
        module.getPlugin().getServer().getScheduler().runTaskAsynchronously(module.getPlugin(), () -> {
            module.getPhasesHandler().getDataStore().save();
            Message.SAVE_DATA.send(sender, (System.currentTimeMillis() - startTime));
        });
    }

    @Override
    public List<String> tabComplete(OneBlockModule module, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
