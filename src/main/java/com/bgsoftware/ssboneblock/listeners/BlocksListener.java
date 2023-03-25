package com.bgsoftware.ssboneblock.listeners;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

public final class BlocksListener implements Listener {

    private final OneBlockModule plugin;

    private boolean fakeBreakEvent = false;

    public BlocksListener(OneBlockModule plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onOneBlockBreak(BlockBreakEvent e) {
        if (fakeBreakEvent)
            return;

        Island island = SuperiorSkyblockAPI.getIslandAt(e.getBlock().getLocation());

        if (island == null || !plugin.getPhasesHandler().canHaveOneBlock(island))
            return;

        Block block = e.getBlock();
        Location blockLocation = block.getLocation();

        Location oneBlockLocation = plugin.getSettings().blockOffset.applyToLocation(
                island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));

        if (!oneBlockLocation.equals(blockLocation))
            return;

        e.setCancelled(true);

        boolean shouldDropItems;

        try {
            fakeBreakEvent = true;
            BlockBreakEvent fakeEvent = new BlockBreakEvent(e.getBlock(), e.getPlayer());
            Bukkit.getPluginManager().callEvent(fakeEvent);

            if (fakeEvent.isCancelled())
                return;

            try {
                shouldDropItems = fakeEvent.isDropItems();
            } catch (Throwable error) {
                shouldDropItems = false;
            }
        } finally {
            fakeBreakEvent = false;
        }

        Block underBlock = block.getRelative(BlockFace.DOWN);
        boolean barrierPlacement = underBlock.getType() == Material.AIR;

        if (barrierPlacement)
            underBlock.setType(Material.BARRIER);

        ItemStack inHandItem = e.getPlayer().getItemInHand();
        blockLocation.add(0, 1, 0);
        World blockWorld = block.getWorld();

        if (shouldDropItems) {
            Collection<ItemStack> drops = block.getDrops(inHandItem);

            if (block.getState() instanceof InventoryHolder) {
                Inventory inventory = ((InventoryHolder) block.getState()).getInventory();
                Collections.addAll(drops, inventory.getContents());
                inventory.clear();
            }

            drops.stream().filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                    .forEach(itemStack -> blockWorld.dropItemNaturally(blockLocation, itemStack));
        }

        if (e.getExpToDrop() > 0) {
            ExperienceOrb orb = blockWorld.spawn(blockLocation, ExperienceOrb.class);
            orb.setExperience(e.getExpToDrop());
        }

        if (inHandItem != null && inHandItem.getType() != Material.AIR)
            plugin.getNMSAdapter().simulateToolBreak(e.getPlayer(), e.getBlock());

        plugin.getPhasesHandler().runNextAction(island, e.getPlayer());

        if (barrierPlacement)
            underBlock.setType(Material.AIR);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onOneBlockPhysics(BlockPhysicsEvent e) {
        Island island = SuperiorSkyblockAPI.getIslandAt(e.getBlock().getLocation());

        if (island == null || !plugin.getPhasesHandler().canHaveOneBlock(island))
            return;

        Location oneBlockLocation = plugin.getSettings().blockOffset.applyToLocation(
                island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));

        if (!oneBlockLocation.equals(e.getBlock().getLocation()))
            return;

        if (e.getChangedType() == Material.AIR) {
            Bukkit.getScheduler().runTaskLater(plugin.getJavaPlugin(), () ->
                    plugin.getPhasesHandler().runNextAction(island, null), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(chunk);

        if (island == null || NextPhaseTimer.getTimer(island) != null)
            return;

        Location oneBlockLocation = plugin.getSettings().blockOffset.applyToLocation(
                island.getCenter(World.Environment.NORMAL).subtract(0.5, 0, 0.5));

        if (oneBlockLocation.getBlockX() >> 4 != chunk.getX() || oneBlockLocation.getBlockZ() >> 4 != chunk.getZ())
            return;

        if (oneBlockLocation.getBlock().getType() == Material.BEDROCK)
            plugin.getPhasesHandler().runNextAction(island, null);
    }

}
