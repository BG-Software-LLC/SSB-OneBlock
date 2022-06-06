package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Action {

    protected static final OneBlockModule plugin = OneBlockModule.getPlugin();

    protected final BlockOffset offsetPosition;

    protected Action(BlockOffset offsetPosition){
        this.offsetPosition = offsetPosition;
    }

    public abstract void run(Location location, Island island, Player player);

}
