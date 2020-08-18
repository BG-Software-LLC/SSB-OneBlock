package com.bgsoftware.ssboneblock.utils;

import com.bgsoftware.ssboneblock.OneBlockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;

public final class LocationUtils {

    private static final OneBlockPlugin plugin = OneBlockPlugin.getPlugin();

    public static Location getOneBlock(Island island){
        return plugin.getSettings().blockOffset.add(island.getCenter(World.Environment.NORMAL));
    }

}
