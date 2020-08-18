package com.bgsoftware.ssboneblock.nms;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.CommandAbstract;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.EnumHand;
import net.minecraft.server.v1_9_R2.IBlockData;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.MojangsonParser;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.TileEntityChest;
import net.minecraft.server.v1_9_R2.World;
import net.minecraft.server.v1_9_R2.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class NMSAdapter_v1_9_R2 implements NMSAdapter {

    @Override
    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    @Override
    public void setChestName(Location chest, String name) {
        World world = ((CraftWorld) chest.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
        TileEntityChest tileEntityChest = (TileEntityChest) world.getTileEntity(blockPosition);
        assert tileEntityChest != null;
        tileEntityChest.a(name);
    }

    @Override
    public void setBlock(Location location, Material type, byte data, String nbt) {
        assert location.getWorld() != null;

        World worldServer = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        worldServer.s(blockPosition);

        org.bukkit.block.Block bukkitBlock = location.getBlock();
        bukkitBlock.setType(type);
        if(data > 0)
            //noinspection deprecation
            bukkitBlock.setData(data);

        if(nbt != null) {
            try {
                Block block = worldServer.getType(blockPosition).getBlock();
                //noinspection deprecation
                IBlockData blockData = block.fromLegacyData(CommandAbstract.a(nbt, 0, 15));
                worldServer.setTypeAndData(blockPosition, blockData, 2);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void applyNBTToEntity(org.bukkit.entity.LivingEntity bukkitEntity, String nbt) {
        try {
            NBTTagCompound tagCompound = MojangsonParser.parse(nbt);
            ((CraftLivingEntity) bukkitEntity).getHandle().a(tagCompound);
        }catch(Exception ex){
            ex.printStackTrace();
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
        if (itemStack.count == 0)
            entityPlayer.a(EnumHand.MAIN_HAND, null);
    }

}
