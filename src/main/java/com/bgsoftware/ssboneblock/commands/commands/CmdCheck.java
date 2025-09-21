package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.commands.ICommand;
import com.bgsoftware.ssboneblock.lang.Message;
import com.bgsoftware.ssboneblock.phases.IslandPhaseData;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class CmdCheck implements ICommand {

    @Override
    public String getLabel() {
        return "check";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "check [" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getPermission() {
        return "oneblock.check";
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
    public void perform(OneBlockModule module, CommandSender sender, String[] args) {
        performCheck(module, sender, args);
    }

    @Override
    public List<String> tabComplete(OneBlockModule module, CommandSender sender, String[] args) {
        return performTabComplete(args);
    }

    static void performCheck(OneBlockModule module, CommandSender sender, String[] args) {
        Island targetIsland;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must specify a player's name.");
                return;
            }

            targetIsland = SuperiorSkyblockAPI.getPlayer((Player) sender).getIsland();
        } else {
            SuperiorPlayer targetPlayer = SuperiorSkyblockAPI.getPlayer(args[1]);
            targetIsland = targetPlayer == null ? SuperiorSkyblockAPI.getGrid().getIsland(args[1]) : targetPlayer.getIsland();
        }

        if (targetIsland == null) {
            Message.INVALID_ISLAND.send(sender, args.length == 1 ? sender.getName() : args[1]);
            return;
        }

        IslandPhaseData islandPhaseData = module.getPhasesHandler().getDataStore().getPhaseData(targetIsland, false);

        int phaseLevel = islandPhaseData == null ? 0 : islandPhaseData.getPhaseLevel();
        int phaseBlock = islandPhaseData == null ? 0 : islandPhaseData.getPhaseBlock();

        Message.PHASE_STATUS.send(sender, phaseLevel + 1, phaseBlock);
    }

    static List<String> performTabComplete(String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                SuperiorPlayer onlinePlayer = SuperiorSkyblockAPI.getPlayer(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(player.getName());
                    if (!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        return list;
    }

}
