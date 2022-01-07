package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.actions.container.SetContainerAction;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.utils.BlockPosition;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class SetBlockAction extends Action {

    private final Material type;
    private final byte data;
    private final SetContainerAction containerAction;
    private final String nbt;

    private SetBlockAction(Material type, byte data, JsonObject container, BlockPosition offsetPosition, String nbt, PhasesHandler phasesHandler) {
        super(offsetPosition);
        this.type = type;
        this.data = data;
        this.nbt = plugin.getNMSAdapter().isLegacy() ? removeBrackets(nbt) : nbt;
        this.containerAction = container == null ? null : SetContainerAction.fromJson(container, phasesHandler);
    }

    @Override
    public void run(Location location, Island island, Player player) {
        if (offsetPosition != null)
            location = offsetPosition.add(location);

        Block block = location.getBlock();

        plugin.getNMSAdapter().setBlock(location, type, data, nbt);

        if (containerAction != null)
            containerAction.run(block.getState());

        island.handleBlockPlace(Key.of(type, data), 1);
    }

    public static SetBlockAction fromJson(JsonObject jsonObject, PhasesHandler phasesHandler) {
        String block = jsonObject.get("block").getAsString();
        byte materialData = jsonObject.has("data") ? jsonObject.get("data").getAsByte() : 0;
        return new SetBlockAction(Material.valueOf(block.toUpperCase()),
                materialData, jsonObject.getAsJsonObject("container"),
                JsonUtils.getBlockPosition(jsonObject.get("offset")),
                jsonObject.has("nbt") ? (plugin.getNMSAdapter().isLegacy() ? "" : block) +
                        jsonObject.get("nbt").getAsString() : null,
                phasesHandler);
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
