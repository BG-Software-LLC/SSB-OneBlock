package com.bgsoftware.ssboneblock.actions.container;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class ContainerItem {

    private final ItemStack templateItem;
    private final int slot, min, max;

    public ContainerItem(ItemStack templateItem, int slot, int min, int max) {
        this.templateItem = templateItem.clone();
        this.slot = slot;
        this.min = min;
        this.max = max;
    }

    public int getSlot() {
        return slot;
    }

    public boolean hasSlot() {
        return slot > 0 && slot < 27;
    }

    public ItemStack buildItem() {
        int amount = min >= max ? min : ThreadLocalRandom.current().nextInt(min, max);
        ItemStack itemStack = templateItem.clone();
        itemStack.setAmount(amount);
        return itemStack;
    }

}
