package com.bgsoftware.ssboneblock.nms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface NMSAdapter {

    default boolean isLegacy(){
        return true;
    }

    void sendActionBar(Player player, String message);

    void setChestName(Location chest, String name);

    void setBlock(Location location, Material type, byte data, String nbt);

    void applyNBTToEntity(LivingEntity entity, String nbt);

    void simulateToolBreak(Player player, Block block);

}
