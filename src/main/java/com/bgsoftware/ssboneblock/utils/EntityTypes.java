package com.bgsoftware.ssboneblock.utils;

import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;

public class EntityTypes {

    @Nullable
    public static final EntityType WIND_CHARGE = getEntityType("WIND_CHARGE");
    @Nullable
    public static final EntityType BREEZE_WIND_CHARGE = getEntityType("BREEZE_WIND_CHARGE");

    private EntityTypes() {

    }

    @Nullable
    private static EntityType getEntityType(String... names) {
        for(String name : names) {
            try {
                return EntityType.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return null;
    }

}
