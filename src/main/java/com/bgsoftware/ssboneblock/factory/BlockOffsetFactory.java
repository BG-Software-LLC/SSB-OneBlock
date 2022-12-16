package com.bgsoftware.ssboneblock.factory;

import com.bgsoftware.ssboneblock.error.ParsingException;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

public final class BlockOffsetFactory {

    private BlockOffsetFactory() {

    }

    @Nullable
    public static BlockOffset createOffset(@Nullable JsonElement offsetElement) throws ParsingException {
        if (offsetElement == null)
            return null;

        if (!(offsetElement instanceof JsonPrimitive))
            throw new ParsingException("Offset section cannot be of type " + offsetElement.getClass().getName());

        String offset = offsetElement.getAsString();

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
