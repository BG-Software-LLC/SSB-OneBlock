package com.bgsoftware.ssboneblock.utils;

import org.bukkit.Location;

public final class BlockPosition {

    private final double x, y, z;

    public BlockPosition(String offset){
        String[] split = offset.split(", ");
        this.x = Double.parseDouble(split[0]);
        this.y = Double.parseDouble(split[1]);
        this.z = Double.parseDouble(split[2]);
    }

    public Location add(Location origin){
        return origin.clone().add(x, y, z);
    }

}
