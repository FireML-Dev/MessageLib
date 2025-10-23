package uk.firedev.messagelib.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

    protected ComponentMessage() {}

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

    public static ComponentSingleMessage componentMessage(@NotNull String message, @NotNull MessageType messageType, @NotNull MiniMessage miniMessage) {
        return componentMessage(
            Utils.processString(message, miniMessage),
            messageType
        );
    }

    public static ComponentSingleMessage componentMessage(@NotNull String message) {
        return componentMessage(message, MessageType.CHAT);
    }

    public static ComponentSingleMessage componentMessage(@NotNull String message, @NotNull MiniMessage miniMessage) {
        return componentMessage(message, MessageType.CHAT, miniMessage);
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

    public static ComponentListMessage componentMessage(@NotNull List<?> message, @NotNull MessageType messageType, @NotNull MiniMessage miniMessage) {
        return new ComponentListMessage(
            message.stream()
                .flatMap(object -> ObjectProcessor.process(object, miniMessage).stream())
                .toList(),
            messageType
        );
    }

    public static ComponentListMessage componentMessage(@NotNull List<?> message) {
        return componentMessage(message, MessageType.CHAT);
    }

    public static ComponentListMessage componentMessage(@NotNull List<?> message, @NotNull MiniMessage miniMessage) {
        return componentMessage(message, MessageType.CHAT, miniMessage);
    }

    // Ambiguous Messages - Could be single or list.

    public static ComponentMessage componentMessage(@NotNull ConfigLoader<?> loader, @NotNull String path) {
        return Utils.getFromConfig(loader, path);
    }

    public static ComponentMessage componentMessage(@NotNull ConfigLoader<?> loader, @NotNull String path, @NotNull String def) {
        ComponentMessage message = Utils.getFromConfig(loader, path);
        return message == null ? componentMessage(def) : message;
    }

    public static ComponentMessage componentMessage(@NotNull ConfigLoader<?> loader, @NotNull String path, @NotNull Component def) {
        ComponentMessage message = Utils.getFromConfig(loader, path);
        return message == null ? componentMessage(def) : message;
    }

    // Abstract Things

    /**
     * Turns this ComponentMessage into a ComponentSingleMessage.
     */
    public ComponentSingleMessage toSingleMessage() {
        if (this instanceof ComponentSingleMessage singleMessage) {
            return singleMessage;
        } else if (this instanceof ComponentListMessage listMessage) {
            return new ComponentSingleMessage(
                Component.join(JoinConfiguration.newlines(), listMessage.get()),
                listMessage.messageType()
            );
        } else {
            throw new IllegalArgumentException("Invalid ComponentMessage instance provided.");
        }
    }

    /**
     * Turns this ComponentMessage into a ComponentListMessage.
     */
    public ComponentListMessage toListMessage() {
        if (this instanceof ComponentSingleMessage singleMessage) {
            return new ComponentListMessage(
                List.of(singleMessage.get()),
                singleMessage.messageType()
            );
        } else if (this instanceof ComponentListMessage listMessage) {
            return listMessage;
        } else {
            throw new IllegalArgumentException("Invalid ComponentMessage instance provided.");
        }
    }

    /**
     * Gets the MiniMessage instance used to parse strings in this ComponentMessage.
     * @return The MiniMessage instance.
     */
    public abstract @NotNull MiniMessage miniMessage();

    /**
     * Overrides the MiniMessage instance used to parse strings in this ComponentMessage.
     * @param miniMessage The MiniMessage instance to use.
     * @return A new ComponentMessage with the updated MiniMessage instance.
     */
    public abstract ComponentMessage miniMessage(@NotNull MiniMessage miniMessage);

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
    public abstract void send(@NotNull List<? extends Audience> audienceList);

    /**
     * Sends the message to an array of Audiences.
     *
     * @param audiences The array of Audiences to send the message to. If the array is empty or null, nothing happens.
     */
    public abstract void send(@Nullable Audience... audiences);

    /**
     * Broadcasts the message to all players on the server.
     */
    public abstract void broadcast();

}
