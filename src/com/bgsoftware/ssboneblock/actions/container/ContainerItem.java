package com.bgsoftware.ssboneblock.actions.container;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class ContainerItem {

    private final Material type;
    private final short durability;
    private final int slot, min, max;

    public ContainerItem(Material type, short durability, int slot, int min, int max){
        this.type = type;
        this.durability = durability;
        this.slot = slot;
        this.min = min;
        this.max = max;
    }

    public int getSlot() {
        return slot;
    }

    public boolean hasSlot(){
        return slot > 0 && slot < 27;
    }

    public ItemStack buildItem(){
        int amount = min >= max ? min : ThreadLocalRandom.current().nextInt(min, max);
        return new ItemStack(type, amount, durability);
    }

}
