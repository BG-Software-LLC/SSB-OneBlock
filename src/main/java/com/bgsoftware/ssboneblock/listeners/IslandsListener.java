package com.bgsoftware.ssboneblock.listeners;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.PostIslandCreateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class IslandsListener implements Listener {

    private final OneBlockModule module;

    public IslandsListener(OneBlockModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDelete(IslandDisbandEvent e) {
        module.getPhasesHandler().getDataStore().removeIsland(e.getIsland());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreate(PostIslandCreateEvent e) {
        module.getPhasesHandler().runNextAction(e.getIsland(), e.getPlayer());
    }


}
