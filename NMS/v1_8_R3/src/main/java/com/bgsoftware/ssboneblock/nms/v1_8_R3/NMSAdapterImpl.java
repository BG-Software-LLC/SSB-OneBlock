package com.bgsoftware.ssboneblock.nms.v1_8_R3;

import com.bgsoftware.ssboneblock.nms.NMSAdapter;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.CommandAbstract;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MojangsonParser;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntityChest;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public final class NMSAdapterImpl implements NMSAdapter {

    @Override
    public SimpleCommandMap getCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    @Override
    public void setChestName(Location chest, String name) {
        World world = ((CraftWorld) chest.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
        TileEntityChest tileEntityChest = (TileEntityChest) world.getTileEntity(blockPosition);
        tileEntityChest.a(name);
    }

    @Override
    public void setBlock(Location location, Material type, byte data, String nbt) {
        assert location.getWorld() != null;

        World worldServer = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());


        if (nbt == null) {
            worldServer.t(blockPosition);
            org.bukkit.block.Block bukkitBlock = location.getBlock();
            bukkitBlock.setType(type);
            if (data > 0)
                //noinspection deprecation
                bukkitBlock.setData(data);
        } else try {
            Block block = worldServer.getType(blockPosition).getBlock();
            IBlockData blockData = block.fromLegacyData(CommandAbstract.a(nbt, 0, 15));

            TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
            if (tileEntity instanceof IInventory) {
                ((IInventory) tileEntity).l();
            }

            worldServer.setTypeAndData(blockPosition, blockData, 2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public org.bukkit.entity.Entity spawnEntityFromNbt(org.bukkit.entity.EntityType entityType, Location location, String nbt) {
        try {
            NBTTagCompound tagCompound = MojangsonParser.parse(nbt);
            tagCompound.setString("id", entityType.name());

            WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();

            Entity entity = EntityTypes.a(tagCompound, worldServer);

            if (entity != null) {
                entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

                worldServer.addEntity(entity);

                Entity currEntity = entity;
                for (NBTTagCompound currCompound = tagCompound; currEntity != null && currCompound.hasKeyOfType("Riding", 10); currCompound = currCompound.getCompound("Riding")) {
                    Entity newEntity = EntityTypes.a(currCompound.getCompound("Riding"), worldServer);
                    if (newEntity != null) {
                        newEntity.setPositionRotation(location.getX(), location.getY(), location.getZ(), newEntity.yaw, newEntity.pitch);
                        worldServer.addEntity(newEntity);
                        currEntity.mount(newEntity);
                    }

                    currEntity = newEntity;
                }

                return entity.getBukkitEntity();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public org.bukkit.inventory.ItemStack applyNBTToItem(org.bukkit.inventory.ItemStack bukkitItem, String nbt) {
        try {
            NBTTagCompound tagCompound = MojangsonParser.parse(nbt);
            ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
            nmsItem.setTag(tagCompound);
            return CraftItemStack.asBukkitCopy(nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return bukkitItem;
        }
    }

    @Override
    public void simulateToolBreak(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock) {
        EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();

        ItemStack itemStack = entityPlayer.bZ();

        WorldServer worldServer = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        IBlockData blockData = worldServer.getType(blockPosition);

        itemStack.a(worldServer, blockData.getBlock(), blockPosition, entityPlayer);
        if (itemStack.count == 0)
            entityPlayer.ca();
    }

}
