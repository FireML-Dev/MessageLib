package uk.firedev.messagelib.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.ObjectProcessor;
import uk.firedev.messagelib.Utils;
import uk.firedev.messagelib.config.ConfigLoader;
import uk.firedev.messagelib.replacer.Replacer;

import java.util.List;
import java.util.Map;

public abstract class ComponentMessage {

    public static final Component ROOT = Component.empty()
        .applyFallbackStyle(
            Style.style()
                .color(NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .build()
        );

    // Single Messages

    public static ComponentSingleMessage componentMessage(@NotNull Component message, @NotNull MessageType messageType) {
        return new ComponentSingleMessage(message, messageType);
    }

    public static ComponentSingleMessage componentMessage(@NotNull Component message) {
        return componentMessage(message, MessageType.CHAT);
    }

    public static ComponentSingleMessage componentMessage(@NotNull Object object, @NotNull MessageType messageType) {
        return componentMessage(
            Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(object)),
            messageType
        );
    }

    public static ComponentSingleMessage componentMessage(@NotNull Object object) {
        return componentMessage(object, MessageType.CHAT);
    }

    public static ComponentSingleMessage componentMessage(@NotNull String message, @NotNull MessageType messageType) {
        return componentMessage(
            Utils.processString(message),
            messageType
        );
    }

    public static ComponentSingleMessage componentMessage(@NotNull String message) {
        return componentMessage(message, MessageType.CHAT);
    }

    // List Messages

    public static ComponentListMessage componentMessage(@NotNull List<?> message, @NotNull MessageType messageType) {
        return new ComponentListMessage(
            message.stream()
                .flatMap(object -> ObjectProcessor.process(object).stream())
                .toList(),
            messageType
        );
    }

    public static ComponentListMessage componentMessage(@NotNull List<?> message) {
        return componentMessage(message, MessageType.CHAT);
    }

    // Ambiguous Messages - Could be single or list.

    public static ComponentMessage componentMessage(@NotNull ConfigLoader<?> loader, @NotNull String path) {
        return Utils.getFromConfig(loader, path);
    }

    // Abstract Things

    /**
     * Creates a copy of this ComponentMessage.
     *
     * @return A new ComponentMessage that is a copy of this one.
     */
    public abstract ComponentMessage createCopy();

    /**
     * Gets the MessageType of this message.
     *
     * @return The MessageType of this message.
     */
    public abstract MessageType messageType();

    /**
     * Sets the MessageType of this message.
     *
     * @param messageType The MessageType to set.
     * @return A new ComponentMessage with the updated MessageType.
     */
    public abstract ComponentMessage messageType(@NotNull MessageType messageType);

    /**
     * Appends to the current message.
     *
     * @param append The object to append. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the appended content.
     */
    public abstract ComponentMessage append(@NotNull Object append);

    /**
     * Prepends to the current message.
     *
     * @param prepend The object to prepend. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the prepended content.
     */
    public abstract ComponentMessage prepend(@NotNull Object prepend);

    /**
     * Replaces all instances of the specified placeholder with the specified replacement.
     * @param placeholder The placeholder to replace.
     * @param replacement The replacement object. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the replacements made.
     */
    public abstract ComponentMessage replace(@NotNull String placeholder, @Nullable Object replacement);

    /**
     * Replaces all instances of the specified placeholders with the specified replacements.
     * @param replacements A map of placeholders to replacements. Explicitly supports {@link Component} and {@link ComponentSingleMessage} as values. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the replacements made.
     */
    public abstract ComponentMessage replace(@NotNull Map<String, ?> replacements);

    /**
     * Applies the specified Replacer to the message.
     * @param replacer The Replacer to apply.
     * @return A new ComponentMessage with the replacements made.
     */
    public abstract ComponentMessage replace(@Nullable Replacer replacer);

    /**
     * Parses PlaceholderAPI placeholders in the message for the specified player.
     * If PlaceholderAPI is not installed, the message is returned unchanged.
     *
     * @param player The player to parse placeholders for. Can be null for non-player specific placeholders.
     * @return A new ComponentMessage with the parsed placeholders.
     */
    public abstract ComponentMessage parsePlaceholderAPI(@Nullable OfflinePlayer player);

    /**
     * Checks if the underlying plain text is empty.
     * @return True if the underlying plain text is empty, false otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Gets the length of the underlying plain text.
     * @return The length of the underlying plain text.
     */
    public abstract int getLength();

    /**
     * Sends the message to the specified Audience.
     *
     * @param audience The Audience to send the message to. If null, nothing happens.
     */
    public abstract void send(@Nullable Audience audience);

    /**
     * Sends the message to a list of Audiences.
     *
     * @param audienceList The list of Audiences to send the message to. If the list is empty, nothing happens.
     */
    public abstract void send(@NotNull List<Audience> audienceList);

    /**
     * Broadcasts the message to all players on the server.
     */
    public abstract void broadcast();

}
