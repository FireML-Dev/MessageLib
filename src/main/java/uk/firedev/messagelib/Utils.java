package uk.firedev.messagelib;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.firedev.messagelib.config.ConfigLoader;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.MessageType;
import uk.firedev.messagelib.placeholders.PAPITagResolver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final Logger LOGGER = LoggerFactory.getLogger("MessageLib");
    public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();
    public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER_SECTION = LegacyComponentSerializer.builder()
        .character('§')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();

    public static boolean isLegacy(@NotNull String message) {
        if (message.isEmpty()) {
            return false;
        }
        if (!MessageLibSettings.get().isEnableLegacy()) {
            return false;
        }
        // If no MiniMessage tags get stripped, the message is assumed to be legacy.
        return MiniMessage.miniMessage().stripTags(message).equals(message);
    }

    /**
     * Processes a String into a Component, detecting whether it's Legacy or MiniMessage format.
     * @param message The message to process.
     * @return The processed Component.
     */
    public static @NotNull Component processString(@NotNull String message) {
        if (message.isEmpty()) {
            return Component.empty();
        }
        if (isLegacy(message)) {
            return LEGACY_COMPONENT_SERIALIZER.deserialize(message);
        } else {
            return MessageLibSettings.get().getMiniMessage().deserialize(message);
        }
    }

    public static @Nullable ComponentMessage getFromConfig(@NotNull ConfigLoader<?> loader, @NotNull String path) {
        ConfigLoader<?> section = loader.getSection(path);
        if (section == null) {
            return fromObject(loader.getObject(path));
        }
        String messageType = section.getString("type");
        MessageType type = MessageType.getFromString(messageType);

        ComponentMessage finalMessage = fromObject(section.getObject("message"));
        return finalMessage != null ? finalMessage.messageType(type) : null;
    }

    private static @Nullable ComponentMessage fromObject(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof List<?> list) {
            return ComponentMessage.componentMessage(list);
        }
        return ComponentMessage.componentMessage(object.toString());
    }

    public static Component parsePlaceholderAPI(@NotNull Component component, @Nullable OfflinePlayer player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return component;
        }
        MiniMessage mm = MiniMessage.miniMessage();
        String input = mm.serialize(component);

        if (!input.contains("%")) {
            return component;
        }

        Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(input);
        String result = matcher.replaceAll("<papi:$1>");

        return mm.deserialize(result, PAPITagResolver.get(player));
    }

    public static boolean isEmpty(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component).isEmpty();
    }

    /**
     * Logs a throwable to console with your provided message.
     * @param message The message to show alongside the throwable.
     */
    public static void debug(@NotNull String message) {
        if (!MessageLibSettings.get().isAllowDebug()) {
            return;
        }
        final String errorMessage = "DEBUG - " + message;
        LOGGER.error(errorMessage, new Throwable());
    }

}
