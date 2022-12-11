package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.error.ParsingException;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomAction extends Action {

    private final Action[] possibilities;

    private RandomAction(Action[] possibilities) {
        super(null);
        this.possibilities = possibilities;
    }

    @Override
    public void run(Location location, Island island, Player player) {
        if (possibilities.length > 0)
            possibilities[ThreadLocalRandom.current().nextInt(possibilities.length)].run(location, island, player);
    }

    public static Optional<Action> fromJson(JsonObject jsonObject, PhasesHandler phasesHandler, String fileName) throws ParsingException {
        JsonElement possibilitiesElement = jsonObject.get("possibilities");
        Action[] possibilities;

        if (possibilitiesElement instanceof JsonArray) {
            possibilities = JsonUtils.getActionsArray((JsonArray) possibilitiesElement, phasesHandler, fileName);
        } else if (possibilitiesElement instanceof JsonPrimitive) {
            String possibilitiesFileName = possibilitiesElement.getAsString();
            JsonArray jsonArray = phasesHandler.getPossibilities(possibilitiesFileName);

            if (jsonArray == null) {
                throw new ParsingException("Invalid possibilities file " + possibilitiesFileName + ".");
            }

            possibilities = JsonUtils.getActionsArray(jsonArray, phasesHandler, possibilitiesFileName);
        } else {
            throw new ParsingException("Missing \"possibilities\" section.");
        }

        return possibilities.length == 0 ? Optional.empty() : Optional.of(new RandomAction(possibilities));
    }

}
