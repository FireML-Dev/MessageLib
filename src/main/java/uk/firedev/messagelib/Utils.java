package uk.firedev.messagelib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.config.ConfigLoader;
import uk.firedev.messagelib.message.ComponentMessage;
import uk.firedev.messagelib.message.MessageType;

public class Utils {

    public static boolean isLegacy(@NotNull String message) {
        if (message.isEmpty()) {
            return false;
        }
        // If no MiniMessage tags get stripped, the message is assumed to be legacy.
        return MiniMessage.miniMessage().stripTags(message).equals(message);
    }

    // Suppressing deprecation warnings as we use Spigot's ChatColor.
    @SuppressWarnings("deprecation")
    public static @NotNull Component processString(@NotNull String message) {
        if (message.isEmpty()) {
            return Component.empty();
        }
        if (isLegacy(message)) {
            String processedMessage = ChatColor.translateAlternateColorCodes('&', message);
            return LegacyComponentSerializer.legacySection().deserialize(processedMessage);
        } else {
            return MiniMessage.miniMessage().deserialize(message);
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
        // TODO: implement list messages
        return ComponentMessage.componentMessage(object.toString());
    }

    public static Component getComponentFromObject(@Nullable Object object) {
        if (object == null) {
            return Component.empty();
        }
        if (object instanceof Component component) {
            return component;
        } else if (object instanceof ComponentMessage componentMessage) {
            return componentMessage.get();
        } else {
            return processString(object.toString());
        }
    }

}
