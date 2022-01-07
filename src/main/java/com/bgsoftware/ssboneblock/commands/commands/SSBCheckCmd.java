package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SSBCheckCmd implements SuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("oneblock");
    }

    @Override
    public String getPermission() {
        return "superior.island.oneblock";
    }

    @Override
    public String getUsage(Locale locale) {
        return "check [player-name/island-name]";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Check one-block progress of a player.";
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
    public void execute(SuperiorSkyblock superiorSkyblock, CommandSender sender, String[] args) {
        CmdCheck.performCheck(OneBlockModule.getPlugin(), sender, args);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock superiorSkyblock, CommandSender sender, String[] args) {
        return CmdCheck.performTabComplete(args);
    }
}
