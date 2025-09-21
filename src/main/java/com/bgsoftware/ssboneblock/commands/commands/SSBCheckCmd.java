package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.lang.Message;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class SSBCheckCmd implements SuperiorCommand {

    private static final OneBlockModule module = OneBlockModule.getModule();

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("oneblock");
    }

    @Override
    public String getPermission() {
        return "superior.island.oneblock";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "oneblock [" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_CHECK.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean displayCommand() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        CmdCheck.performCheck(module, sender, args);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return CmdCheck.performTabComplete(args);
    }
}
