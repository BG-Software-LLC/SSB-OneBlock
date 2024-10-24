package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class MultiAction extends Action {

    private final Action[] actions;

    public MultiAction(Action[] actions){
        super(null);
        this.actions = actions;
    }

    @Override
    public void run(Location location, Island island, OfflinePlayer player) {
        for (Action action : actions)
            action.run(location, island, player);
    }
}
