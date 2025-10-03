package com.bgsoftware.ssboneblock.nms.v1_21_4;

import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class NMSAdapterImpl extends com.bgsoftware.ssboneblock.nms.v1_21_4.AbstractNMSAdapter {

    @Override
    protected BlockState setBlockWithNBT(ServerLevel serverLevel, BlockPos blockPos, String nbt) throws Exception {
        BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(
                serverLevel.holderLookup(Registries.BLOCK), new StringReader(nbt), true);
        BlockInput blockInput = new BlockInput(blockResult.blockState(), blockResult.properties().keySet(),
                blockResult.nbt());

        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        Clearable.tryClear(blockEntity);

        blockInput.place(serverLevel, blockPos, 2);

        return blockInput.getState();
    }

    @Override
    protected EntityType<?> convertBukkitEntityType(org.bukkit.entity.EntityType entityType) {
        return CraftEntityType.bukkitToMinecraft(entityType);
    }

    @Override
    protected Entity loadEntity(CompoundTag compoundTag, ServerLevel serverLevel, Location location) {
        return EntityType.loadEntityRecursive(compoundTag, serverLevel, EntitySpawnReason.COMMAND, x -> {
            x.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            x.spawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;
            return x;
        });
    }

    @Override
    protected void addEntity(Entity entity, ServerLevel serverLevel) {
        serverLevel.tryAddFreshEntityWithPassengers(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    protected void applyNBTToItem(ItemStack itemStack, String nbt) throws Exception {
        ItemParser itemParser = new ItemParser(CraftRegistry.getMinecraftRegistry());
        ItemParser.ItemResult itemResult = itemParser.parse(new StringReader(nbt));
        DataComponentPatch components = itemResult.components();
        itemStack.applyComponents(components);
    }
}
