package com.bgsoftware.ssboneblock.listeners;

import com.bgsoftware.ssboneblock.OneBlockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class IslandsListener implements Listener {

    private final OneBlockPlugin plugin;

    public IslandsListener(OneBlockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDelete(IslandDisbandEvent e){
        plugin.getPhasesHandler().clearIsland(e.getIsland());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreate(IslandCreateEvent e){
        plugin.getPhasesHandler().runNextAction(e.getIsland(), null);
    }


}
