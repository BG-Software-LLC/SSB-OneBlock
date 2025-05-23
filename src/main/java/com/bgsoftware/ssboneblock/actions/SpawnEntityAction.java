package com.bgsoftware.ssboneblock.actions;

import com.bgsoftware.ssboneblock.error.ParsingException;
import com.bgsoftware.ssboneblock.factory.BlockOffsetFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

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
    public void run(Location location, Island island, @Nullable SuperiorPlayer superiorPlayer) {
        if (offsetPosition != null)
            location = offsetPosition.applyToLocation(location);

        location = location.clone().add(0.5, 2, 0.5);

        if (this.nbt != null) {
            module.getNMSAdapter().spawnEntityFromNbt(this.entityType, location, this.nbt);
        } else {
            location.getWorld().spawnEntity(location, this.entityType);
        }
    }

    public static Optional<Action> fromJson(JsonObject jsonObject) throws ParsingException {
        JsonElement typeElement = jsonObject.get("type");

        if (!(typeElement instanceof JsonPrimitive))
            throw new ParsingException("Missing \"type\" section.");

        String entityTypeRaw = typeElement.getAsString();
        EntityType entityType;

        try {
            entityType = EntityType.valueOf(entityTypeRaw.toUpperCase());
        } catch (IllegalArgumentException error) {
            throw new ParsingException("Cannot parse `" + entityTypeRaw + "` to a valid entity type.");
        }

        return Optional.of(new SpawnEntityAction(entityType,
                BlockOffsetFactory.createOffset(jsonObject.get("offset")),
                jsonObject.has("nbt") ? jsonObject.get("nbt").getAsString() : null));
    }

}
