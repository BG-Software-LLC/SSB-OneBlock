package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public abstract class Action {

    protected static final OneBlockModule plugin = OneBlockModule.getPlugin();

    @Nullable
    protected final BlockOffset offsetPosition;

    protected Action(@Nullable BlockOffset offsetPosition){
        this.offsetPosition = offsetPosition;
    }

    public abstract void run(Location location, Island island, OfflinePlayer player);

}
