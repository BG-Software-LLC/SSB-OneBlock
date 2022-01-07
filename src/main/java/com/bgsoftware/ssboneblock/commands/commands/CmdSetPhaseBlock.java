package com.bgsoftware.ssboneblock.commands.commands;

import com.bgsoftware.ssboneblock.Locale;
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

public final class CmdSetPhaseBlock implements ICommand {

    @Override
    public String getLabel() {
        return "setphaseblock";
    }

    @Override
    public String getUsage() {
        return "oneblock setphaseblock <player-name/island-name> <phase-block>";
    }

    @Override
    public String getPermission() {
        return "oneblock.setphaseblock";
    }

    @Override
    public String getDescription() {
        return "Set the phase-block for a specific player.";
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

        if(island == null){
            Locale.INVALID_ISLAND.send(sender, args[1]);
            return;
        }

        int phaseBlock;

        try{
            phaseBlock = Integer.parseInt(args[2]);
        }catch(Exception ex){
            Locale.INVALID_ISLAND.send(sender, args[2]);
            return;
        }

        if(phaseBlock <= 0 || !plugin.getPhasesHandler().setPhaseBlock(island, phaseBlock - 1, island.getOwner().asPlayer())){
            Locale.SET_PHASE_BLOCK_FAILURE.send(sender, phaseBlock);
        }
        else{
            Locale.SET_PHASE_BLOCK_SUCCESS.send(sender, args[1], phaseBlock);
        }
    }

    @Override
    public List<String> tabComplete(OneBlockModule plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 2) {
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
