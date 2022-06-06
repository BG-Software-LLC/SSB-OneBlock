package com.bgsoftware.ssboneblock.factory;

import com.bgsoftware.ssboneblock.error.ParsingException;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;

public final class BlockOffsetFactory {

    private BlockOffsetFactory() {

    }

    public static BlockOffset createOffset(String offset) throws ParsingException {
        String[] blockOffsetSections = offset.split(", ");

        if (blockOffsetSections.length != 3)
            throw new ParsingException("Cannot parse `" + offset + "` to a valid offset.");

        return createOffset((int) Double.parseDouble(blockOffsetSections[0]),
                (int) Double.parseDouble(blockOffsetSections[1]),
                (int) Double.parseDouble(blockOffsetSections[2]));
    }

    public static BlockOffset createOffset(int offsetX, int offsetY, int offsetZ) {
        return SuperiorSkyblockAPI.getFactory().createBlockOffset(offsetX, offsetY, offsetZ);
    }

}
