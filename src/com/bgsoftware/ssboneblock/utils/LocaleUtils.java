package com.bgsoftware.ssboneblock.utils;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.regex.Pattern;

public final class LocaleUtils {

    private static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-z]{2}[_|-][A-Z]{2}$");

    public static Locale getLocale(CommandSender sender){
        return sender instanceof Player ? SuperiorSkyblockAPI.getPlayer((Player) sender).getUserLocale() : Locale.US;
    }

    public static Locale getLocale(String str) throws IllegalArgumentException{
        str = str.replace("_", "-");

        Preconditions.checkArgument(LOCALE_PATTERN.matcher(str).matches(), "String " + str + " is not a valid language.");

        String[] numberFormatSections = str.split("-");

        return new Locale(numberFormatSections[0], numberFormatSections[1]);
    }

}
