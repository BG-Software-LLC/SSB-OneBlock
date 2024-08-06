package com.bgsoftware.ssboneblock.utils;

import com.bgsoftware.ssboneblock.OneBlockModule;

import java.io.File;

public class Resources {

    private static final OneBlockModule module = OneBlockModule.getPlugin();

    private Resources() {

    }

    public static void saveResource(String destination) {
        String resourcePath = destination;

        try {
            for (ServerVersion serverVersion : ServerVersion.getByOrder()) {
                String version = serverVersion.name().substring(1);
                if (resourcePath.endsWith(".yml") && module.getResource(
                        resourcePath.replace(".yml", version + ".yml")) != null) {
                    resourcePath = resourcePath.replace(".yml", version + ".yml");
                    break;
                }
            }

            File file = new File(module.getModuleFolder(), resourcePath);
            module.saveResource(resourcePath);

            if (!destination.equals(resourcePath)) {
                File dest = new File(module.getModuleFolder(), destination);
                //noinspection ResultOfMethodCallIgnored
                file.renameTo(dest);
            }
        } catch (Exception error) {
            module.getLogger().warning("An unexpected error occurred while saving resource:");
            error.printStackTrace();
        }
    }


}
