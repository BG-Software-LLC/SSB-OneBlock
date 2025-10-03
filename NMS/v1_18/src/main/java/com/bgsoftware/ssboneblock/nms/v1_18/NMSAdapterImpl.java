package com.bgsoftware.ssboneblock.nms.v1_18;

import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class NMSAdapterImpl extends com.bgsoftware.ssboneblock.nms.v1_18.AbstractNMSAdapter {

    @Override
    protected BlockState setBlockWithNBT(ServerLevel serverLevel, BlockPos blockPos, String nbt) throws Exception {
        BlockStateParser blockStateParser = new BlockStateParser(new StringReader(nbt), false).parse(true);
        BlockState blockState = blockStateParser.getState();
        if (blockState != null) {
            BlockInput blockInput = new BlockInput(blockState, blockStateParser.getProperties().keySet(),
                    blockStateParser.getNbt());

            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            Clearable.tryClear(blockEntity);

            blockInput.place(serverLevel, blockPos, 2);
        }
        return blockState;
    }

    @Override
    protected EntityType<?> convertBukkitEntityType(org.bukkit.entity.EntityType entityType) {
        return EntityType.byString(entityType.name()).orElseThrow();
    }

    @Override
    protected Entity loadEntity(CompoundTag compoundTag, ServerLevel serverLevel, Location location) {
        return EntityType.loadEntityRecursive(compoundTag, serverLevel, x -> {
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
        ItemParser itemParser = new ItemParser(new StringReader(nbt), false).parse();
        CompoundTag compoundTag = itemParser.getNbt();
        if (compoundTag != null) {
            itemStack.setTag(compoundTag);
        }
    }
}
