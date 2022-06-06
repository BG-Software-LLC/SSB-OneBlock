package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.error.ParsingException;
import com.bgsoftware.ssboneblock.factory.BlockOffsetFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Optional;

public final class SpawnEntityAction extends Action {


    private final EntityType entityType;
    private final String nbt;

    private SpawnEntityAction(EntityType entityType, @Nullable BlockOffset offsetPosition, String nbt) {
        super(offsetPosition);
        this.entityType = entityType;
        this.nbt = nbt;
    }

    @Override
    public void run(Location location, Island island, Player player) {
        if (offsetPosition != null)
            location = offsetPosition.applyToLocation(location);

        Entity entity = location.getWorld().spawnEntity(location.clone().add(0.5, 2, 0.5), entityType);
        if (nbt != null && entity instanceof LivingEntity)
            plugin.getNMSAdapter().applyNBTToEntity((LivingEntity) entity, nbt);
    }

    public static Optional<Action> fromJson(JsonObject jsonObject) throws ParsingException {
        String entityTypeRaw = jsonObject.get("type").getAsString();
        EntityType entityType;

        try {
            entityType = EntityType.valueOf(entityTypeRaw.toUpperCase());
        } catch (IllegalArgumentException error) {
            throw new ParsingException("Cannot parse `" + entityTypeRaw + "` to a valid entity type.");
        }

        JsonElement offsetElement = jsonObject.get("offset");

        return Optional.of(new SpawnEntityAction(entityType,
                offsetElement == null ? null : BlockOffsetFactory.createOffset(offsetElement.getAsString()),
                jsonObject.has("nbt") ? jsonObject.get("nbt").getAsString() : null));
    }

}
