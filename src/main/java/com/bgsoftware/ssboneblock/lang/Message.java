package com.bgsoftware.ssboneblock.lang;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.ssboneblock.OneBlockModule;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public enum Message {

    COMMAND_USAGE,
    HELP_COMMAND_HEADER,
    HELP_COMMAND_LINE,
    HELP_COMMAND_FOOTER,
    INVALID_ISLAND,
    INVALID_NUMBER,
    ISLAND_MISSING_BLOCK,
    NO_MORE_PHASES,
    NO_PERMISSION,
    PHASE_PROGRESS,
    PHASE_STATUS,
    SET_PHASE_BLOCK_FAILURE,
    SET_PHASE_BLOCK_SUCCESS,
    SET_PHASE_FAILURE,
    SET_PHASE_SUCCESS;

    private static final MessagesService MESSAGES_SERVICE = Bukkit.getServicesManager().getRegistration(MessagesService.class).getProvider();
    private static final IMessageComponent EMPTY_COMPONENT = MESSAGES_SERVICE.newBuilder().build();

    private final Map<Locale, IMessageComponent> messages = new HashMap<>();

    public void send(@Nullable SuperiorPlayer superiorPlayer, Object... objects) {
        if (superiorPlayer != null)
            send(superiorPlayer.asPlayer(), superiorPlayer.getUserLocale(), objects);
    }

    public void send(@Nullable CommandSender sender, Object... objects) {
        if (sender != null)
            send(sender, LocaleUtils.getLocale(sender), objects);
    }

    public void send(@Nullable CommandSender sender, java.util.Locale locale, Object... args) {
        if (sender != null)
            messages.getOrDefault(locale, EMPTY_COMPONENT).sendMessage(sender, args);
    }

    private void setMessage(java.util.Locale locale, IMessageComponent messageComponent) {
        messages.put(locale, messageComponent);
    }

    private static final OneBlockModule module = OneBlockModule.getModule();

    public static void reload() {
        OneBlockModule.log("Loading messages started...");
        long startTime = System.currentTimeMillis();

        File langFolder = new File(module.getModuleFolder(), "lang");

        if (!langFolder.exists()) {
            module.saveResource("lang/de-DE.yml");
            module.saveResource("lang/en-US.yml");
            module.saveResource("lang/es-ES.yml");
            module.saveResource("lang/fr-FR.yml");
            module.saveResource("lang/it-IT.yml");
            module.saveResource("lang/iw-IL.yml");
            module.saveResource("lang/pl-PL.yml");
            module.saveResource("lang/vi-VN.yml");
            module.saveResource("lang/zh-CN.yml");
        }

        int messagesAmount = 0;
        boolean countMessages = true;

        for (File langFile : Objects.requireNonNull(langFolder.listFiles())) {
            String fileName = langFile.getName().split("\\.")[0];
            java.util.Locale fileLocale;

            try {
                fileLocale = LocaleUtils.getLocale(fileName);
            } catch (IllegalArgumentException ex) {
                OneBlockModule.log("&cThe language \"" + fileName + "\" is invalid. Please correct the file name.");
                continue;
            }
            CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(langFile);

            try (InputStream inputStream = findResourceOrNull("lang/" + langFile.getName(), "lang/en-US.yml")) {
                if (inputStream != null)
                    cfg.syncWithConfig(langFile, inputStream, "lang/en-US.yml");
            } catch (IOException error) {
                throw new RuntimeException(error);
            }

            for (Message message : values()) {
                message.setMessage(fileLocale, MESSAGES_SERVICE.parseComponent(cfg, message.name()));
                if (countMessages)
                    messagesAmount++;
            }

            countMessages = false;
        }

        OneBlockModule.log(" - Found " + messagesAmount + " messages in the language files.");
        OneBlockModule.log("Loading messages done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    private static InputStream findResourceOrNull(String... resourceNames) {
        for (String resourceName : resourceNames) {
            try {
                return module.getResource(resourceName);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return null;
    }

}
