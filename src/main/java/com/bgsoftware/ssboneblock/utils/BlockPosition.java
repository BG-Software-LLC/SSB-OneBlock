package com.bgsoftware.ssboneblock.utils;

import com.bgsoftware.ssboneblock.error.ParsingException;
import org.bukkit.Location;

public final class BlockPosition {

    private final double x, y, z;

    public BlockPosition(String offset) throws ParsingException {
        String[] split = offset.split(", ");

        if (split.length != 3)
            throw new ParsingException("Cannot parse `" + offset + "` to a valid offset.");

        this.x = Double.parseDouble(split[0]);
        this.y = Double.parseDouble(split[1]);
        this.z = Double.parseDouble(split[2]);
    }

    public BlockPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location add(Location origin) {
        return origin.clone().add(x, y, z);
    }

}
