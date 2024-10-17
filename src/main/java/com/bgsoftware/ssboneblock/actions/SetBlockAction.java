package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.actions.container.SetContainerAction;
import com.bgsoftware.ssboneblock.error.ParsingException;
import com.bgsoftware.ssboneblock.factory.BlockOffsetFactory;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Optional;

public final class SetBlockAction extends Action {

    private final Material type;
    private final byte data;
    private final SetContainerAction containerAction;
    private final String nbt;

    private SetBlockAction(Material type, byte data, JsonObject container, @Nullable BlockOffset offsetPosition,
                           String nbt, PhasesHandler phasesHandler, String fileName) {
        super(offsetPosition);
        this.type = type;
        this.data = data;
        this.nbt = plugin.getNMSAdapter().isLegacy() ? removeBrackets(nbt) : nbt;
        this.containerAction = container == null ? null : SetContainerAction.fromJson(container, phasesHandler, fileName);
    }

    @Override
    public void run(Location location, Island island, Player player) {
        if (offsetPosition != null)
            location = offsetPosition.applyToLocation(location);

        Block block = location.getBlock();

        plugin.getNMSAdapter().setBlock(location, type, data, nbt);

        if (containerAction != null)
            containerAction.run(block.getState());

        Key oldKey = Key.of(block);
        Key newKey = Key.of(type, data);
        if (oldKey.equals(newKey))
            return;

        island.handleBlockBreak(oldKey, 1, false);
        island.handleBlockPlace(newKey, 1, false);
    }

    public static Optional<Action> fromJson(JsonObject jsonObject, PhasesHandler phasesHandler, String fileName) throws ParsingException {
        JsonElement blockElement = jsonObject.get("block");

        if (!(blockElement instanceof JsonPrimitive))
            throw new ParsingException("Missing \"block\" section.");

        String block = blockElement.getAsString();
        byte materialData = jsonObject.has("data") ? jsonObject.get("data").getAsByte() : 0;
        Material type;

        try {
            type = Material.valueOf(block.toUpperCase());
        } catch (IllegalArgumentException error) {
            throw new ParsingException("Cannot parse `" + block + "` to a valid material type.");
        }

        return Optional.of(new SetBlockAction(type,
                materialData, jsonObject.getAsJsonObject("container"),
                BlockOffsetFactory.createOffset(jsonObject.get("offset")),
                jsonObject.has("nbt") ? (plugin.getNMSAdapter().isLegacy() ? "" : block) +
                        jsonObject.get("nbt").getAsString() : null,
                phasesHandler, fileName));
    }

    private static String removeBrackets(String str) {
        if (str != null) {
            if (str.startsWith("["))
                str = str.substring(1);
            if (str.endsWith("]"))
                str = str.substring(0, str.length() - 1);
        }
        return str;
    }

}
