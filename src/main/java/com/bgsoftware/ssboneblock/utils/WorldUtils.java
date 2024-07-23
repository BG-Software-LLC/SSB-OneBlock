package com.bgsoftware.ssboneblock.utils;

import org.bukkit.inventory.Inventory;

public class WorldUtils {

    private WorldUtils() {

    }

    public static boolean shouldDropInventory(Inventory inventoryHolder) {
        if (ServerVersion.isAtLeast(ServerVersion.v1_9)) {
            if (inventoryHolder instanceof org.bukkit.block.ShulkerBox)
                return false;
        }

        return true;
    }

}
