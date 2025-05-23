package com.bgsoftware.ssboneblock.utils;

import org.bukkit.Bukkit;

import java.util.Arrays;

public enum ServerVersion {

    v1_7(17),
    v1_8(18),
    v1_9(19),
    v1_10(110),
    v1_11(111),
    v1_12(112),
    v1_13(113),
    v1_14(114),
    v1_15(115),
    v1_16(116),
    v1_17(117),
    v1_18(118),
    v1_19(119),
    v1_20(120),
    v1_21(121);

    private static final ServerVersion currentVersion;
    private static final String bukkitVersion;
    private static final boolean legacy;

    static {
        bukkitVersion = Bukkit.getBukkitVersion().split("-")[0];
        String[] sections = bukkitVersion.split("\\.");
        currentVersion = ServerVersion.valueOf("v" + sections[0] + "_" + sections[1]);
        legacy = isLessThan(ServerVersion.v1_13);
    }

    private final int code;

    ServerVersion(int code) {
        this.code = code;
    }


    public static boolean isAtLeast(ServerVersion serverVersion) {
        return currentVersion.code >= serverVersion.code;
    }

    public static boolean isLessThan(ServerVersion serverVersion) {
        return currentVersion.code < serverVersion.code;
    }

    public static boolean isEquals(ServerVersion serverVersion) {
        return currentVersion.code == serverVersion.code;
    }

    public static boolean isLegacy() {
        return legacy;
    }

    public static String getBukkitVersion() {
        return bukkitVersion;
    }

    public static ServerVersion[] getByOrder() {
        ServerVersion[] versions = Arrays.copyOfRange(values(), 0, currentVersion.ordinal() + 1);

        for (int i = 0; i < versions.length / 2; i++) {
            ServerVersion temp = versions[i];
            versions[i] = versions[versions.length - i - 1];
            versions[versions.length - i - 1] = temp;
        }

        return versions;
    }

}
