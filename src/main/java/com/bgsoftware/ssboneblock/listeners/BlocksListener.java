package com.bgsoftware.ssboneblock.listeners;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.task.NextPhaseTimer;
import com.bgsoftware.ssboneblock.utils.WorldUtils;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BlocksListener implements Listener {

    private final OneBlockModule module;

    public BlocksListener(OneBlockModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onOneBlockBreak(BlockBreakEvent e) {
        if (e.getClass().equals(FakeBlockBreakEvent.class))
            return;

        Block block = e.getBlock();
        Location blockLocation = block.getLocation();

        WorldUtils.lookupOneBlock(blockLocation, (oneBlockLocation, island) -> {
            e.setCancelled(true);

            FakeBlockBreakEvent fakeEvent = new FakeBlockBreakEvent(e.getBlock(), e.getPlayer());
            Bukkit.getPluginManager().callEvent(fakeEvent);

            if (fakeEvent.isCancelled())
                return;

            boolean shouldDropItems;
            try {
                shouldDropItems = fakeEvent.isDropItems();
            } catch (Throwable error) {
                shouldDropItems = false;
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

                if (blockState instanceof InventoryHolder &&
                        WorldUtils.shouldDropInventory((InventoryHolder) blockState)) {
                    Inventory inventory = ((InventoryHolder) blockState).getInventory();
                    Collections.addAll(drops, inventory.getContents());
                    inventory.clear();
                }

                drops.forEach(itemStack -> {
                    if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0)
                        blockWorld.dropItemNaturally(blockLocation, itemStack);
                });
            }

            if (e.getExpToDrop() > 0) {
                ExperienceOrb orb = blockWorld.spawn(blockLocation, ExperienceOrb.class);
                orb.setExperience(e.getExpToDrop());
            }

            if (inHandItem != null && inHandItem.getType() != Material.AIR)
                module.getNMSAdapter().simulateToolBreak(e.getPlayer(), e.getBlock());

            SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(e.getPlayer());
            block.setType(Material.AIR);
            module.getPhasesHandler().runNextAction(island, superiorPlayer);

            if (barrierPlacement)
                underBlock.setType(Material.AIR);
        });

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewFallingBlock(EntitySpawnEvent e) {
        if (e.getEntityType() != EntityType.FALLING_BLOCK)
            return;

        Location blockLocation = new Location(e.getLocation().getWorld(), e.getLocation().getBlockX(),
                e.getLocation().getBlockY(), e.getLocation().getBlockZ());

        WorldUtils.lookupOneBlock(blockLocation, (oneBlockLocation, island) -> {
            Bukkit.getScheduler().runTaskLater(module.getPlugin(), () ->
                    module.getPhasesHandler().runNextAction(island, null), 20L);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onOneBlockBurn(BlockBurnEvent e) {
        WorldUtils.lookupOneBlock(e.getBlock().getLocation(), (oneBlockLocation, island) -> {
            Bukkit.getScheduler().runTaskLater(module.getPlugin(), () ->
                    module.getPhasesHandler().runNextAction(island, null), 20L);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        WorldUtils.lookupOneBlock(event.getChunk(), (oneBlockLocation, island) -> {
            if (NextPhaseTimer.getTimer(island) != null)
                return;

            if (oneBlockLocation.getBlock().getType() == Material.BEDROCK)
                module.getPhasesHandler().runNextAction(island, null);
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        onPistonMoveInternal(event.getBlock(), event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        onPistonMoveInternal(event.getBlock(), event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent e) {
        Player sourcePlayer = null;
        if (e.getEntity() instanceof TNTPrimed) {
            Entity sourceEntity = ((TNTPrimed) e.getEntity()).getSource();
            if (sourceEntity instanceof Player)
                sourcePlayer = (Player) sourceEntity;
        }

        SuperiorPlayer superiorPlayer = sourcePlayer == null ? null :
                module.getPlugin().getPlayers().getSuperiorPlayer(sourcePlayer);

        AtomicBoolean foundOneBlock = new AtomicBoolean(false);

        for (Block block : e.blockList()) {
            if (foundOneBlock.get())
                break;

            WorldUtils.lookupOneBlock(block.getLocation(), (oneBlockLocation, island) -> {
                if (!foundOneBlock.getAndSet(true)) {
                    Bukkit.getScheduler().runTaskLater(module.getPlugin(), () ->
                            module.getPhasesHandler().runNextAction(island, superiorPlayer), 1L);
                }
            });
        }
    }

    private void onPistonMoveInternal(Block pistonBlock, List<Block> blockList, Cancellable event) {
        if (module.getSettings().pistonsInteraction)
            return;

        WorldUtils.lookupOneBlock(pistonBlock.getLocation(), (oneBlockLocation, island) -> {
            for (Block block : blockList) {
                if (block.getLocation().equals(oneBlockLocation)) {
                    event.setCancelled(true);
                    return;
                }
            }
        });
    }

    private static class FakeBlockBreakEvent extends BlockBreakEvent {

        FakeBlockBreakEvent(Block block, Player player) {
            super(block, player);
        }

    }

}
