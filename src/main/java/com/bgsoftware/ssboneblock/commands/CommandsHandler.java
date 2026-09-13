package com.bgsoftware.ssboneblock.commands;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.commands.commands.CmdCheck;
import com.bgsoftware.ssboneblock.commands.commands.CmdReload;
import com.bgsoftware.ssboneblock.commands.commands.CmdSave;
import com.bgsoftware.ssboneblock.commands.commands.CmdSetPhase;
import com.bgsoftware.ssboneblock.commands.commands.CmdSetPhaseBlock;
import com.bgsoftware.ssboneblock.lang.LocaleUtils;
import com.bgsoftware.ssboneblock.lang.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CommandsHandler extends Command {

    private final Map<String, ICommand> subCommands = new LinkedHashMap<>();
    private final OneBlockModule module;

    public CommandsHandler(OneBlockModule module, String label) {
        super(label,
                "Main command for the plugin.",
                "/" + label + " <command>",
                Collections.singletonList("ob")
        );
        this.module = module;

        registerCommand(new CmdCheck());
        registerCommand(new CmdReload());
        registerCommand(new CmdSave());
        registerCommand(new CmdSetPhase());
        registerCommand(new CmdSetPhaseBlock());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        java.util.Locale locale = LocaleUtils.getLocale(sender);

        if (args.length > 0) {
            ICommand subCommand = this.subCommands.get(args[0].toUpperCase(Locale.ENGLISH));
            if (subCommand != null) {
                if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                    Message.NO_PERMISSION.send(sender);
                    return false;
                }
                if (args.length < subCommand.getMinArgs() || args.length > subCommand.getMaxArgs()) {
                    Message.COMMAND_USAGE.send(sender, label + " " + subCommand.getUsage(locale));
                    return false;
                }
                subCommand.perform(module, sender, args);
                return true;
            }
        }

        //Checking that the player has permission to use at least one of the commands.
        for (ICommand subCommand : subCommands.values()) {
            if (sender.hasPermission(subCommand.getPermission())) {
                //Player has permission
                Message.HELP_COMMAND_HEADER.send(sender);

                for (ICommand cmd : subCommands.values()) {
                    if (sender.hasPermission(subCommand.getPermission()))
                        Message.HELP_COMMAND_LINE.send(sender, label + " " + cmd.getUsage(locale), cmd.getDescription(locale));
                }

                Message.HELP_COMMAND_FOOTER.send(sender);
                return false;
            }
        }

        Message.NO_PERMISSION.send(sender);

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
        if (args.length > 0) {
            ICommand subCommand = this.subCommands.get(args[0].toUpperCase(Locale.ENGLISH));
            if (subCommand != null) {
                if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                    return Collections.emptyList();
                }
                return subCommand.tabComplete(module, sender, args);
            }
        }

        List<String> list = new LinkedList<>();

        for (ICommand subCommand : subCommands.values())
            if (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))
                if (subCommand.getLabel().startsWith(args[0]))
                    list.add(subCommand.getLabel());

        return list;
    }

    private void registerCommand(ICommand command) {
        subCommands.put(command.getLabel().toUpperCase(Locale.ENGLISH), command);
    }

}
