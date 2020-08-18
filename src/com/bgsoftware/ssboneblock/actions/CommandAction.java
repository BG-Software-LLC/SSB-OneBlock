package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.utils.BlockPosition;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class CommandAction extends Action {

    private final List<String> commands;

    private CommandAction(List<String> commands, BlockPosition offsetPosition){
        super(offsetPosition);
        this.commands = commands;
    }

    @Override
    public void run(Location location, Island island, Player player) {
        if(offsetPosition != null)
            location = offsetPosition.add(location);

        for(String command : commands){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                    .replace("{player}", player == null ? "null" : player.getName())
                    .replace("{world}", location.getWorld().getName())
                    .replace("{x}", location.getBlockX() + "")
                    .replace("{y}", location.getBlockY() + "")
                    .replace("{z}", location.getBlockZ() + "")
            );
        }
    }

    public static CommandAction fromJson(JsonObject jsonObject){
        JsonElement jsonElement = jsonObject.get("execute");
        List<String> commands = new ArrayList<>();

        if(jsonElement instanceof JsonArray){
            ((JsonArray) jsonElement).forEach(_jsonElement -> commands.add(_jsonElement.getAsString()));
        }else{
            commands.add(jsonElement.getAsString());
        }

        return new CommandAction(commands, JsonUtils.getBlockPosition(jsonObject.get("offset")));
    }

}
