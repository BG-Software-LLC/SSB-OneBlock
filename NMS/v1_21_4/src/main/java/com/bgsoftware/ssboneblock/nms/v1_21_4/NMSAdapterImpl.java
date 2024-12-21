package com.bgsoftware.ssboneblock.nms.v1_21_4;

import com.bgsoftware.ssboneblock.nms.NMSAdapter;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
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
        World bukkitWorld = chest.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot set name of chest in null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);

        if (blockEntity instanceof ChestBlockEntity chestBlockEntity)
            chestBlockEntity.name = CraftChatMessage.fromString(name)[0];
    }

    @Override
    public void setBlock(Location location, Material type, byte data, String nbt) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot set block in a null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        serverLevel.removeBlockEntity(blockPos);

        location.getBlock().setType(type);

        if (nbt != null) {
            try {
                BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(
                        serverLevel.holderLookup(Registries.BLOCK), new StringReader(nbt), false);
                BlockInput blockInput = new BlockInput(blockResult.blockState(), blockResult.properties().keySet(),
                        blockResult.nbt());
                blockInput.place(serverLevel, blockPos, 2);
                serverLevel.blockUpdated(blockPos, blockInput.getState().getBlock());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void applyNBTToEntity(org.bukkit.entity.LivingEntity bukkitEntity, String nbt) {
        try {
            CompoundTag compoundTag = CompoundTagArgument.compoundTag().parse(new StringReader(nbt));
            ((CraftLivingEntity) bukkitEntity).getHandle().readAdditionalSaveData(compoundTag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack applyNBTToItem(org.bukkit.inventory.ItemStack bukkitItem, String nbt) {
        try {
            CompoundTag compoundTag = CompoundTagArgument.compoundTag().parse(new StringReader(nbt));
            ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
            compoundTag = (CompoundTag) nmsItem.save(MinecraftServer.getServer().registryAccess(), compoundTag);
            nmsItem = ItemStack.parse(MinecraftServer.getServer().registryAccess(), compoundTag).orElseThrow();
            return CraftItemStack.asBukkitCopy(nmsItem);
        } catch (Exception ex) {
            ex.printStackTrace();
            return bukkitItem;
        }
    }

    @Override
    public void simulateToolBreak(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock) {
        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();

        ItemStack itemStack = serverPlayer.getMainHandItem();

        ServerLevel serverLevel = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        BlockPos blockPos = new BlockPos(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        BlockState blockState = serverLevel.getBlockState(blockPos);

        itemStack.mineBlock(serverLevel, blockState, blockPos, serverPlayer);
    }

}

