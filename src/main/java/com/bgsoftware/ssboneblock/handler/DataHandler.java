package com.bgsoftware.ssboneblock.handler;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class DataHandler {

    private final OneBlockModule plugin;

    public DataHandler(OneBlockModule plugin){
        this.plugin = plugin;
    }

    public void loadDatabase(){
        File file = new File(plugin.getDataFolder(), "database.json");

        if(!file.exists()){
            try {
                file.createNewFile();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return;
        }

        try(FileReader reader = new FileReader(file)){
            JsonArray jsonArray = JsonUtils.getGson().fromJson(reader, JsonArray.class);
            for(JsonElement islandData : jsonArray){
                try {
                    plugin.getPhasesHandler().loadIslandData((JsonObject) islandData);
                }catch(Exception ex){
                    OneBlockModule.log("Failed to parse data for element: " + islandData);
                    ex.printStackTrace();
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void saveDatabase(){
        File file = new File(plugin.getDataFolder(), "database.json");

        if(!file.exists()){
            try {
                file.createNewFile();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        try(FileWriter writer  = new FileWriter(file)){
            writer.write(JsonUtils.getGson().toJson(plugin.getPhasesHandler().saveIslandData()));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
