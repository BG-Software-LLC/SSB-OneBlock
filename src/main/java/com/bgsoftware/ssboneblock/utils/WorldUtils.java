package com.bgsoftware.ssboneblock.utils;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.InventoryHolder;

public class WorldUtils {
    private static final OneBlockModule module = OneBlockModule.getModule();

    private WorldUtils() {

    }

    public static boolean shouldDropInventory(InventoryHolder inventoryHolder) {
        if (ServerVersion.isAtLeast(ServerVersion.v1_9)) {
            if (inventoryHolder instanceof org.bukkit.block.ShulkerBox)
                return false;
        }

        return true;
    }

    public static Island getOneBlockIsland(Location location) {
        Island island = SuperiorSkyblockAPI.getIslandAt(location);

        if (island == null || !module.getPhasesHandler().canHaveOneBlock(island))
            return null;

        Location oneBlockLocation = module.getSettings().blockOffset.applyToLocation(
                island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));

        return oneBlockLocation.equals(location) ? island : null;
    }

    public static Location getOneBlockLocation(Location location) {
        Island island = SuperiorSkyblockAPI.getIslandAt(location);

        if (island == null || !module.getPhasesHandler().canHaveOneBlock(island))
            return null;

        return module.getSettings().blockOffset.applyToLocation(
                island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));
    }

}
