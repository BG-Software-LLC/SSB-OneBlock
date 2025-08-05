package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.commands.ICommand;
import com.bgsoftware.ssboneblock.lang.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class CmdReload implements ICommand {

    @Override
    public String getLabel() {
        return "reload";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "oneblock.reload";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_RELOAD.getMessage(locale);
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
            module.onReload(module.getPlugin());
            Message.RELOAD_FILES.send(sender, (System.currentTimeMillis() - startTime));
        });
    }

    @Override
    public List<String> tabComplete(OneBlockModule module, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
