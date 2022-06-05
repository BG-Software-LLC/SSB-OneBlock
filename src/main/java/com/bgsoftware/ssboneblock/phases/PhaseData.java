package com.bgsoftware.ssboneblock.phases;

import com.bgsoftware.ssboneblock.actions.Action;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Optional;

public final class PhaseData {

    private final String name;
    private final Action[] actions;
    private final short nextPhaseCooldown;

    private PhaseData(String name, Action[] actions, short nextPhaseCooldown) {
        this.name = name;
        this.actions = actions;
        this.nextPhaseCooldown = nextPhaseCooldown;
    }

    public String getName() {
        return name;
    }

    public Action getAction(int block) {
        return block < 0 || block >= actions.length ? null : actions[block];
    }

    public int getActionsSize() {
        return actions.length;
    }

    public short getNextPhaseCooldown() {
        return nextPhaseCooldown;
    }

    public static Optional<PhaseData> fromJson(JsonObject jsonObject, PhasesHandler phasesManager, String fileName) {
        JsonArray actionsArray = jsonObject.getAsJsonArray("actions");

        if (actionsArray == null)
            throw new IllegalArgumentException("File is missing the key \"actions\"!");

        Action[] actions = JsonUtils.getActionsArray(actionsArray, phasesManager, fileName);

        String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : fileName.split("\\.")[0];

        return actions.length == 0 ? Optional.empty() :
                Optional.of(new PhaseData(name, actions, jsonObject.get("next-upgrade-cooldown").getAsShort()));
    }

}
