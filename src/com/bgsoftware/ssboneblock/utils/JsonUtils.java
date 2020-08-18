package com.bgsoftware.ssboneblock.utils;

import com.bgsoftware.ssboneblock.actions.Action;
import com.bgsoftware.ssboneblock.actions.ActionType;
import com.bgsoftware.ssboneblock.actions.CommandAction;
import com.bgsoftware.ssboneblock.actions.MultiAction;
import com.bgsoftware.ssboneblock.actions.RandomAction;
import com.bgsoftware.ssboneblock.actions.SetBlockAction;
import com.bgsoftware.ssboneblock.actions.SpawnEntityAction;
import com.bgsoftware.ssboneblock.actions.container.ContainerPoll;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JsonUtils {

    private static final Gson gson = new GsonBuilder().create();

    public static Gson getGson() {
        return gson;
    }

    public static Action getAction(JsonObject actionObject, PhasesHandler phasesHandler){
        String action = actionObject.get("action").getAsString();
        ActionType actionType;

        try{
            actionType = ActionType.valueOf(action.toUpperCase());
        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException("Invalid action-type \"" + action + "\".");
        }

        switch (actionType) {
            case SET_BLOCK:
                return SetBlockAction.fromJson(actionObject, phasesHandler);
            case RANDOM:
                return RandomAction.fromJson(actionObject, phasesHandler);
            case COMMAND:
                return CommandAction.fromJson(actionObject);
            case SPAWN_ENTITY:
                return SpawnEntityAction.fromJson(actionObject);
            //Never called
            default:
                return null;
        }
    }

    public static Action[] getActionsArray(JsonArray jsonArray, PhasesHandler phasesManager){
        List<Action> actionList = new ArrayList<>();

        for(JsonElement actionElement : jsonArray){
            try {
                JsonObject actionObject = actionElement.getAsJsonObject();
                Action action;

                if (actionObject.has("actions")) {
                    List<Action> multipleActions = new ArrayList<>();
                    for (JsonElement _actionElement : actionObject.getAsJsonArray("actions"))
                        multipleActions.add(JsonUtils.getAction(_actionElement.getAsJsonObject(), phasesManager));
                    action = new MultiAction(multipleActions.toArray(new Action[0]));
                } else {
                    action = JsonUtils.getAction(actionObject, phasesManager);
                }

                int amountOfActions = actionObject.has("amount") ? actionObject.get("amount").getAsInt() : 1;

                for(int i = 0; i < amountOfActions; i++)
                    actionList.add(action);
            }catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }
        }

        return actionList.toArray(new Action[0]);
    }

    public static ContainerPoll[] getContainerItems(JsonArray jsonArray){
        List<ContainerPoll> polls = new ArrayList<>();

        for(JsonElement jsonElement : jsonArray){
            polls.add(ContainerPoll.fromJson((JsonObject) jsonElement));
        }

        return polls.toArray(new ContainerPoll[0]);
    }

    public static BlockPosition getBlockPosition(JsonElement jsonElement){
        return jsonElement == null ? null : new BlockPosition(jsonElement.getAsString());
    }

}
