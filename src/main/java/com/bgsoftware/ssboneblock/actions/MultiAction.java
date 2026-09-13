package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;

public final class MultiAction extends Action {

    private final List<Action> actions;

    public MultiAction(List<Action> actions){
        super(null);
        this.actions = actions;
    }

    @Override
    public void run(Location location, Island island, @Nullable SuperiorPlayer superiorPlayer) {
        for (Action action : actions)
            action.run(location, island, superiorPlayer);
    }
}
