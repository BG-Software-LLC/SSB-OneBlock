package com.bgsoftware.ssboneblock.utils;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class WorldUtils {

    private static final OneBlockModule module = OneBlockModule.getModule();
    private static final boolean isShulkerBoxSupported = ServerVersion.isAtLeast(ServerVersion.v1_9);

    private WorldUtils() {

    }

    public static boolean shouldDropInventory(InventoryHolder inventoryHolder) {
        if (isShulkerBoxSupported && inventoryHolder instanceof org.bukkit.block.ShulkerBox)
            return false;

        return true;
    }

    public static void lookupOneBlock(Chunk chunk, BiConsumer<Location, Island> consumer) {
        List<Island> islands = module.getPlugin().getGrid().getIslandsAt(chunk);
        if (islands.size() != 1)
            return;

        lookupOneBlockWithIsland(islands.get(0), (oneBlockLocation, island) -> {
            if (oneBlockLocation.getBlockX() >> 4 != chunk.getX() || oneBlockLocation.getBlockZ() >> 4 != chunk.getZ())
                consumer.accept(oneBlockLocation, island);
        });
    }

    public static void lookupOneBlock(Location location, BiConsumer<Location, Island> consumer) {
        Island islandAtLocation = module.getPlugin().getGrid().getIslandAt(location);
        lookupOneBlockWithIsland(islandAtLocation, (oneBlockLocation, island) -> {
            if (oneBlockLocation.equals(location))
                consumer.accept(oneBlockLocation, island);
        });
    }

    public static Location getOneBlock(Island island) {
        Location islandCenter = island.getCenter(module.getPlugin().getSettings().getWorlds().getDefaultWorldDimension());
        return module.getSettings().blockOffset.applyToLocation(islandCenter.subtract(0.5, 0, 0.5));
    }

    private static void lookupOneBlockWithIsland(@Nullable Island island,
                                                 BiConsumer<Location, Island> consumer) {
        if (island == null || !module.getPhasesHandler().canHaveOneBlock(island))
            return;

        Location oneBlockLocation = getOneBlock(island);

        consumer.accept(oneBlockLocation, island);
    }

}
