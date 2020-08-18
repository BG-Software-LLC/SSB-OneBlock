package com.bgsoftware.ssboneblock;

import com.bgsoftware.ssboneblock.config.CommentedConfiguration;
import com.bgsoftware.ssboneblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum Locale {

    COMMAND_USAGE,
    HELP_COMMAND_HEADER,
    HELP_COMMAND_LINE,
    HELP_COMMAND_FOOTER,
    INVALID_ISLAND,
    INVALID_NUMBER,
    NO_MORE_PHASES,
    NO_PERMISSION,
    PHASE_PROGRESS,
    PHASE_STATUS,
    SET_PHASE_BLOCK_FAILURE,
    SET_PHASE_BLOCK_SUCCESS,
    SET_PHASE_FAILURE,
    SET_PHASE_SUCCESS;

    private final Map<java.util.Locale, String> messages = new HashMap<>();
    private final String defaultMessage;

    Locale(){
        this(null);
    }

    Locale(String defaultMessage){
        this.defaultMessage = defaultMessage;
    }

    public boolean isEmpty(java.util.Locale locale){
        return messages.getOrDefault(locale, "").isEmpty();
    }

    public String getMessage(java.util.Locale locale, Object... objects){
        if(!isEmpty(locale)) {
            String msg = messages.get(locale);

            for (int i = 0; i < objects.length; i++)
                msg = msg.replace("{" + i + "}", objects[i].toString());

            return msg;
        }

        return defaultMessage;
    }

    public void send(SuperiorPlayer superiorPlayer, Object... objects){
        send(superiorPlayer.asPlayer(), superiorPlayer.getUserLocale(), objects);
    }

    public void send(CommandSender sender, Object... objects){
        send(sender, LocaleUtils.getLocale(sender), objects);
    }

    public void send(CommandSender sender, java.util.Locale locale, Object... objects){
        String message = getMessage(locale, objects);
        if(message != null && sender != null)
            sendMessage(sender, message);
    }

    private void setMessage(java.util.Locale locale, String message){
        if(message == null)
            message = "";
        messages.put(locale, message);
    }

    private static final OneBlockPlugin plugin = OneBlockPlugin.getPlugin();

    public static void reload(){
        OneBlockPlugin.log("Loading messages started...");
        long startTime = System.currentTimeMillis();

        File langFolder = new File(plugin.getDataFolder(), "lang");

        if(!langFolder.exists()){
            plugin.saveResource("lang/en-US.yml", false);
        }

        int messagesAmount = 0;
        boolean countMessages = true;

        for(File langFile : Objects.requireNonNull(langFolder.listFiles())){
            String fileName = langFile.getName().split("\\.")[0];
            java.util.Locale fileLocale;

            try{
                fileLocale = LocaleUtils.getLocale(fileName);
            }catch(IllegalArgumentException ex){
                OneBlockPlugin.log("&cThe language \"" + fileName + "\" is invalid. Please correct the file name.");
                continue;
            }

            CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(langFile);
            InputStream inputStream = plugin.getResource("lang/" + langFile.getName());
            cfg.syncWithConfig(langFile, inputStream == null ? plugin.getResource("lang/en-US.yml") : inputStream, "lang/en-US.yml");

            for(Locale locale : values()){
                locale.setMessage(fileLocale, ChatColor.translateAlternateColorCodes('&', cfg.getString(locale.name(), "")));

                if(countMessages)
                    messagesAmount++;
            }

            countMessages = false;
        }

        OneBlockPlugin.log(" - Found " + messagesAmount + " messages in the language files.");
        OneBlockPlugin.log("Loading messages done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    public static void sendMessage(CommandSender sender, String message){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
