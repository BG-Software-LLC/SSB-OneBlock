package com.bgsoftware.ssboneblock.phases;

import com.bgsoftware.ssboneblock.actions.Action;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Optional;

public final class PhaseData {

    private final Action[] actions;
    private final short nextPhaseCooldown;

    private PhaseData(Action[] actions, short nextPhaseCooldown) {
        this.actions = actions;
        this.nextPhaseCooldown = nextPhaseCooldown;
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

        return actions.length == 0 ? Optional.empty() :
                Optional.of(new PhaseData(actions, jsonObject.get("next-upgrade-cooldown").getAsShort()));
    }

}
