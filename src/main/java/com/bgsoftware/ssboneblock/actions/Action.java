package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.utils.BlockPosition;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Action {

    protected static final OneBlockModule plugin = OneBlockModule.getPlugin();

    protected final BlockPosition offsetPosition;

    protected Action(BlockPosition offsetPosition){
        this.offsetPosition = offsetPosition;
    }

    public abstract void run(Location location, Island island, Player player);

}
