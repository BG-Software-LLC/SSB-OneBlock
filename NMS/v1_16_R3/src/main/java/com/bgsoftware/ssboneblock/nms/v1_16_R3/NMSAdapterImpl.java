package com.bgsoftware.ssboneblock.nms.v1_16_R3;

import com.bgsoftware.ssboneblock.nms.NMSAdapter;
import com.mojang.brigadier.StringReader;
import net.minecraft.server.v1_16_R3.ArgumentBlock;
import net.minecraft.server.v1_16_R3.ArgumentNBTTag;
import net.minecraft.server.v1_16_R3.ArgumentTileLocation;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Clearable;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityChest;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

public final class NMSAdapterImpl implements NMSAdapter {

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public SimpleCommandMap getCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    @Override
    public void setChestName(Location chest, String name) {
        assert chest.getWorld() != null;
        World world = ((CraftWorld) chest.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
        TileEntityChest tileEntityChest = (TileEntityChest) world.getTileEntity(blockPosition);
        assert tileEntityChest != null;
        tileEntityChest.setCustomName(CraftChatMessage.fromString(name)[0]);
    }

    @Override
    public void setBlock(Location location, Material type, byte data, String nbt) {
        assert location.getWorld() != null;

        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (nbt == null) {
            worldServer.removeTileEntity(blockPosition);
            location.getBlock().setType(type);
        } else try {
            ArgumentBlock argumentBlock = new ArgumentBlock(new StringReader(nbt), false).a(true);
            ArgumentTileLocation tileLocation = new ArgumentTileLocation(argumentBlock.getBlockData(),
                    argumentBlock.getStateMap().keySet(), argumentBlock.c());

            TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
            Clearable.a(tileEntity);

            tileLocation.a(worldServer, blockPosition, 2);
            worldServer.update(blockPosition, tileLocation.a().getBlock());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void applyNBTToEntity(org.bukkit.entity.LivingEntity bukkitEntity, String nbt) {
        try {
            NBTTagCompound tagCompound = ArgumentNBTTag.a().parse(new StringReader(nbt));
            ((CraftLivingEntity) bukkitEntity).getHandle().loadData(tagCompound);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack applyNBTToItem(org.bukkit.inventory.ItemStack bukkitItem, String nbt) {
        try {
            NBTTagCompound tagCompound = ArgumentNBTTag.a().parse(new StringReader(nbt));
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

        ItemStack itemStack = entityPlayer.getItemInMainHand();

        WorldServer worldServer = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        IBlockData blockData = worldServer.getType(blockPosition);

        assert itemStack != null;

        itemStack.a(worldServer, blockData, blockPosition, entityPlayer);
    }

}

