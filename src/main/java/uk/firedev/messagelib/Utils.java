package uk.firedev.messagelib;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.config.ConfigLoader;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.MessageType;

import java.util.List;
import java.util.regex.Matcher;

public class Utils {

    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder().postProcessor(component -> component).build();
    public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
        .useUnusualXRepeatedCharacterHexFormat()
        .character('&')
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
        return processString(message, MINI_MESSAGE);
    }

    /**
     * Processes a String into a Component, detecting whether it's Legacy or MiniMessage format.
     * @param message The message to process.
     * @param miniMessage The MiniMessage instance to use for deserialization.
     * @return The processed Component.
     */
    public static @NotNull Component processString(@NotNull String message, @NotNull MiniMessage miniMessage) {
        if (message.isEmpty()) {
            return Component.empty();
        }
        if (isLegacy(message)) {
            return LEGACY_COMPONENT_SERIALIZER.deserialize(message);
        } else {
            return miniMessage.deserialize(message);
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
        String stringMessage = MiniMessage.miniMessage().serialize(component);
        Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(stringMessage);
        while (matcher.find()) {
            // Find matched String
            String matched = matcher.group();
            // Convert to Legacy Component and into a MiniMessage String
            String parsed = MiniMessage.miniMessage().serialize(
                LegacyComponentSerializer.legacySection().deserialize(
                    PlaceholderAPI.setPlaceholders(player, matched)
                )
            );
            // Escape matched String so we don't have issues
            String safeMatched = Matcher.quoteReplacement(matched);
            // Replace all instances of the matched String with the parsed placeholder.
            stringMessage = stringMessage.replaceAll(safeMatched, parsed);
        }
        return MiniMessage.miniMessage().deserialize(stringMessage);
    }

}
