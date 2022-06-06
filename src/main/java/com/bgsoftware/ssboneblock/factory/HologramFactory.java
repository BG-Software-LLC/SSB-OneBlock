package com.bgsoftware.ssboneblock.factory;

import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.api.service.hologram.HologramsService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nullable;

public final class HologramFactory {

    @Nullable
    private static final HologramsService HOLOGRAMS_SERVICE = createService();

    private HologramFactory() {

    }

    @Nullable
    public static Hologram createHologram(Location location) {
        return HOLOGRAMS_SERVICE == null ? null : HOLOGRAMS_SERVICE.createHologram(location);
    }

    @Nullable
    private static HologramsService createService() {
        RegisteredServiceProvider<HologramsService> hologramsServicesProvider =
                Bukkit.getServicesManager().getRegistration(HologramsService.class);
        return hologramsServicesProvider == null ? null : hologramsServicesProvider.getProvider();
    }

}
