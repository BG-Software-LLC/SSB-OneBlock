package com.bgsoftware.ssboneblock.listeners;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.utils.WorldUtils;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

        Block block = e.getBlock();
        Location blockLocation = block.getLocation();
        Island island = WorldUtils.getOneBlockIsland(blockLocation);

        if (island == null)
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
            BlockState blockState = block.getState();

            if (blockState instanceof InventoryHolder) {
                Inventory inventory = ((InventoryHolder) blockState).getInventory();
                if(WorldUtils.shouldDropInventory((InventoryHolder) blockState))){
                    Collections.addAll(drops, inventory.getContents());
                    inventory.clear();
                }
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewFallingBlock(EntitySpawnEvent e) {
        if (e.getEntityType() != EntityType.FALLING_BLOCK)
            return;

        Location blockLocation = new Location(e.getLocation().getWorld(), e.getLocation().getBlockX(),
                e.getLocation().getBlockY(), e.getLocation().getBlockZ());

        Island island = WorldUtils.getOneBlockIsland(blockLocation);

        if (island == null)
            return;

        Bukkit.getScheduler().runTaskLater(plugin.getJavaPlugin(), () ->
                plugin.getPhasesHandler().runNextAction(island, null), 20L);
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        onPistonMoveInternal(event.getBlock(), event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        onPistonMoveInternal(event.getBlock(), event.getBlocks(), event);
    }

    private void onPistonMoveInternal(Block pistonBlock, List<Block> blockList, Cancellable event) {
        if (plugin.getSettings().pistonsInteraction)
            return;

        Location oneBlockLocation = WorldUtils.getOneBlockLocation(pistonBlock.getLocation());

        for (Block block : blockList) {
            if (block.getLocation().equals(oneBlockLocation)) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
