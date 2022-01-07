package com.bgsoftware.ssboneblock.commands;

import com.bgsoftware.ssboneblock.Locale;
import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.commands.commands.CmdCheck;
import com.bgsoftware.ssboneblock.commands.commands.CmdReload;
import com.bgsoftware.ssboneblock.commands.commands.CmdSave;
import com.bgsoftware.ssboneblock.commands.commands.CmdSetPhase;
import com.bgsoftware.ssboneblock.commands.commands.CmdSetPhaseBlock;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class CommandsHandler implements CommandExecutor, TabCompleter {

    private final List<ICommand> subCommands = new ArrayList<>();
    private final OneBlockModule plugin;

    public CommandsHandler(OneBlockModule plugin){
        this.plugin = plugin;
        subCommands.add(new CmdCheck());
        subCommands.add(new CmdReload());
        subCommands.add(new CmdSave());
        subCommands.add(new CmdSetPhase());
        subCommands.add(new CmdSetPhaseBlock());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(args.length > 0){
            for(ICommand subCommand : subCommands) {
                if (subCommand.getLabel().equalsIgnoreCase(args[0])){
                    if(subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())){
                        Locale.NO_PERMISSION.send(sender);
                        return false;
                    }
                    if(args.length < subCommand.getMinArgs() || args.length > subCommand.getMaxArgs()){
                        Locale.COMMAND_USAGE.send(sender, subCommand.getUsage());
                        return false;
                    }
                    subCommand.perform(plugin, sender, args);
                    return true;
                }
            }
        }

        //Checking that the player has permission to use at least one of the commands.
        for(ICommand subCommand : subCommands){
            if(sender.hasPermission(subCommand.getPermission())){
                //Player has permission
                Locale.HELP_COMMAND_HEADER.send(sender);

                for(ICommand cmd : subCommands) {
                    if(sender.hasPermission(subCommand.getPermission()))
                        Locale.HELP_COMMAND_LINE.send(sender, cmd.getUsage(), cmd.getDescription());
                }

                Locale.HELP_COMMAND_FOOTER.send(sender);
                return false;
            }
        }

        Locale.NO_PERMISSION.send(sender);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if(args.length > 0){
            for(ICommand subCommand : subCommands) {
                if (subCommand.getLabel().equalsIgnoreCase(args[0])){
                    if(subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())){
                        return new ArrayList<>();
                    }
                    return subCommand.tabComplete(plugin, sender, args);
                }
            }
        }

        List<String> list = new ArrayList<>();

        for(ICommand subCommand : subCommands)
            if(subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))
                if(subCommand.getLabel().startsWith(args[0]))
                    list.add(subCommand.getLabel());

        return list;
    }
}
