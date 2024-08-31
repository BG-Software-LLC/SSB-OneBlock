package com.bgsoftware.ssboneblock.utils;

import org.bukkit.inventory.InventoryHolder;

public class WorldUtils {

    private WorldUtils() {

    }

    public static boolean shouldDropInventory(InventoryHolder inventoryHolder) {
        if (ServerVersion.isAtLeast(ServerVersion.v1_9)) {
            if (inventoryHolder instanceof org.bukkit.block.ShulkerBox)
                return false;
        }

        return true;
    }

}
