package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.lang.Message;
import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class CmdSetPhase implements ICommand {

    @Override
    public String getLabel() {
        return "setphase";
    }

    @Override
    public String getUsage() {
        return "oneblock setphase <player-name/island-name> <phase-level>";
    }

    @Override
    public String getPermission() {
        return "oneblock.setphase";
    }

    @Override
    public String getDescription() {
        return "Set the phase for a specific player.";
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public void perform(OneBlockModule plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SuperiorSkyblockAPI.getPlayer(args[1]);
        Island island = targetPlayer == null ? SuperiorSkyblockAPI.getGrid().getIsland(args[1]) : targetPlayer.getIsland();

        if (island == null) {
            Message.INVALID_ISLAND.send(sender, args[1]);
            return;
        }

        if(!plugin.getPhasesHandler().canHaveOneBlock(island)) {
            Message.ISLAND_MISSING_BLOCK.send(sender);
            return;
        }

        int phaseLevel;

        try {
            phaseLevel = Integer.parseInt(args[2]);
        } catch (Exception ex) {
            Message.INVALID_NUMBER.send(sender, args[2]);
            return;
        }

        if (phaseLevel <= 0 || !plugin.getPhasesHandler().setPhaseLevel(island, phaseLevel - 1, island.getOwner())) {
            Message.SET_PHASE_FAILURE.send(sender, phaseLevel);
        } else {
            Message.SET_PHASE_SUCCESS.send(sender, args[1], phaseLevel);
        }
    }

    @Override
    public List<String> tabComplete(OneBlockModule plugin, CommandSender sender, String[] args) {
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
