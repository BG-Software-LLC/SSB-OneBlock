package com.bgsoftware.ssboneblock.actions.container;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.ssboneblock.handler.PhasesHandler;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class SetContainerAction {

    private static final OneBlockModule module = OneBlockModule.getModule();

    private final ContainerPoll[] polls;
    private final String name;

    private SetContainerAction(ContainerPoll[] polls, String name) {
        this.polls = polls;
        this.name = name;
    }

    public void run(BlockState blockState) {
        if (!(blockState instanceof InventoryHolder))
            return;

        Inventory inventory = ((InventoryHolder) blockState).getInventory();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (ContainerPoll poll : this.polls)
            poll.run(inventory, random);

        if (name != null && blockState instanceof Chest)
            module.getNMSAdapter().setChestName(blockState.getLocation(), name);
    }

    public static SetContainerAction fromJson(JsonObject jsonObject, PhasesHandler phasesHandler, String fileName) {
        String name = Optional.ofNullable(jsonObject.get("name")).map(nameElement ->
                ChatColor.translateAlternateColorCodes('&', nameElement.getAsString()))
                .orElse(null);
        JsonElement contentsElement = jsonObject.get("contents");
        ContainerPoll[] polls;

        if (contentsElement instanceof JsonArray) {
            polls = JsonUtils.getContainerItems((JsonArray) contentsElement, fileName);
        } else {
            String possibilitiesFileName = contentsElement.getAsString();
            JsonArray jsonArray = phasesHandler.getPossibilities(possibilitiesFileName);

            if (jsonArray == null) {
                throw new IllegalArgumentException("Invalid contents file " + possibilitiesFileName + ".");
            }

            polls = JsonUtils.getContainerItems(jsonArray, fileName);
        }

        return new SetContainerAction(polls, name);
    }

}
