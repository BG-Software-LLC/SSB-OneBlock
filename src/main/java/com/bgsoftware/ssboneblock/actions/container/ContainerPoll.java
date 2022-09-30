package com.bgsoftware.ssboneblock.actions.container;

import com.bgsoftware.ssboneblock.OneBlockModule;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ContainerPoll {

    private static final OneBlockModule module = OneBlockModule.getPlugin();

    private final ContainerItem[] items;
    private final int min, max;

    private ContainerPoll(int min, int max, ContainerItem[] items) {
        this.min = min;
        this.max = max;
        this.items = items;
    }

    public void run(Inventory inventory, ThreadLocalRandom random) {
        if (min == -1 || max == -1) {
            for (ContainerItem containerItem : items) {
                setItem(inventory, containerItem, random);
            }
        } else {
            int itemsAmount = min >= max ? min : random.nextInt(min, max);
            List<ContainerItem> rolledItems = new ArrayList<>(itemsAmount);

            for (int i = 0; i < itemsAmount; i++) {
                ContainerItem containerItem;

                do {
                    containerItem = items[random.nextInt(items.length)];
                } while (rolledItems.contains(containerItem));

                rolledItems.add(containerItem);

                setItem(inventory, containerItem, random);
            }
        }
    }

    public static ContainerPoll fromJson(JsonObject jsonObject, String fileName) {
        List<ContainerItem> containerItems = new ArrayList<>();
        int rollMin = -1, rollMax = -1;

        if (jsonObject.has("rolls")) {
            JsonObject rolls = jsonObject.getAsJsonObject("rolls");
            rollMin = rolls.get("min").getAsInt();
            rollMax = rolls.get("max").getAsInt();
        }

        for (JsonElement itemElement : jsonObject.getAsJsonArray("entries")) {
            JsonObject itemObject = itemElement.getAsJsonObject();

            String materialTypeRaw = itemObject.get("type").getAsString();
            Material type;

            try {
                type = Material.valueOf(materialTypeRaw.toUpperCase());
            } catch (IllegalArgumentException error) {
                OneBlockModule.log("[" + fileName + "] Cannot parse `" + materialTypeRaw + "` to a valid material type.");
                continue;
            }

            short durability = itemObject.has("data") ? itemObject.get("data").getAsShort() : 0;
            int min = itemObject.get("min").getAsInt();
            int max = itemObject.get("max").getAsInt();
            int slot = itemObject.has("slot") ? itemObject.get("slot").getAsInt() : -1;

            ItemStack itemStack = new ItemStack(type, durability);
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemObject.has("name"))
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemObject.get("name").getAsString()));

            if (itemObject.has("lore")) {
                List<String> lore = new ArrayList<>();

                for (JsonElement loreLine : itemObject.get("lore").getAsJsonArray())
                    lore.add(loreLine.getAsString());

                if (!lore.isEmpty())
                    itemMeta.setLore(lore);
            }

            if (itemObject.has("nbt"))
                itemStack = module.getNMSAdapter().applyNBTToItem(itemStack, itemObject.get("nbt").getAsString());

            ContainerItem containerItem = new ContainerItem(itemStack, slot, min, max);

            int amountOfActions = itemObject.has("weight") ? itemObject.get("weight").getAsInt() : 1;
            for (int i = 0; i < amountOfActions; i++)
                containerItems.add(containerItem);
        }

        return new ContainerPoll(rollMin, rollMax, containerItems.toArray(new ContainerItem[0]));
    }

    private static void setItem(Inventory inventory, ContainerItem containerItem, ThreadLocalRandom random) {
        int slot;

        if (containerItem.hasSlot()) {
            slot = containerItem.getSlot();
        } else {
            do {
                slot = random.nextInt(27);
            } while (inventory.getItem(slot) != null);
        }

        inventory.setItem(slot, containerItem.buildItem());
    }

}