package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomAction extends Action {

    private final Action[] possibilities;

    private RandomAction(Action[] possibilities){
        super(null);
        this.possibilities = possibilities;
    }

    @Override
    public void run(Location location, Island island, Player player) {
        if(possibilities.length > 0)
            possibilities[ThreadLocalRandom.current().nextInt(possibilities.length)].run(location, island, player);
    }

    public static RandomAction fromJson(JsonObject jsonObject, PhasesHandler phasesHandler){
        JsonElement possibilitiesElement = jsonObject.get("possibilities");
        Action[] possibilities;

        if(possibilitiesElement instanceof JsonArray){
            possibilities = JsonUtils.getActionsArray((JsonArray) possibilitiesElement, phasesHandler);
        }
        else{
            JsonArray jsonArray = phasesHandler.getPossibilities(possibilitiesElement.getAsString());

            if(jsonArray == null){
                throw new IllegalArgumentException("Invalid possibilities file " + possibilitiesElement.getAsString() + ".");
            }

            possibilities = JsonUtils.getActionsArray(jsonArray, phasesHandler);
        }

        return new RandomAction(possibilities);
    }

}
