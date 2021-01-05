package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.utils.BlockPosition;
import com.bgsoftware.ssboneblock.utils.JsonUtils;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class SpawnEntityAction extends Action {


    private final EntityType entityType;
    private final String nbt;

    private SpawnEntityAction(EntityType entityType, BlockPosition offsetPosition, String nbt){
        super(offsetPosition);
        this.entityType = entityType;
        this.nbt = nbt;
    }

    @Override
    public void run(Location location, Island island, Player player) {
        if(offsetPosition != null)
            location = offsetPosition.add(location);

        Entity entity = location.getWorld().spawnEntity(location.clone().add(0.5, 2, 0.5), entityType);
        if(nbt != null && entity instanceof LivingEntity)
            plugin.getNMSAdapter().applyNBTToEntity((LivingEntity) entity, nbt);
    }

    public static SpawnEntityAction fromJson(JsonObject jsonObject){
        return new SpawnEntityAction(EntityType.valueOf(jsonObject.get("type").getAsString().toUpperCase()),
                JsonUtils.getBlockPosition(jsonObject.get("offset")),
                jsonObject.has("nbt") ? jsonObject.get("nbt").getAsString() : null);
    }

}
