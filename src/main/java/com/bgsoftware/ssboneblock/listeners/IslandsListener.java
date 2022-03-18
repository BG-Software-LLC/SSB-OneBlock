package com.bgsoftware.ssboneblock.listeners;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class IslandsListener implements Listener {

    private final OneBlockModule plugin;

    public IslandsListener(OneBlockModule plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDelete(IslandDisbandEvent e){
        plugin.getPhasesHandler().getPhasesContainer().removeIsland(e.getIsland());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreate(IslandCreateEvent e){
        if(plugin.getPhasesHandler().canHaveOneBlock(e.getIsland()))
            plugin.getPhasesHandler().runNextAction(e.getIsland(), null);
    }


}
