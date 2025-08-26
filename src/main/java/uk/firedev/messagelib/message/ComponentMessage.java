package uk.firedev.messagelib.message;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.Utils;
import uk.firedev.messagelib.config.ConfigLoader;
import uk.firedev.messagelib.replacer.Replacer;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

// NEEDS TO BE IMMUTABLE - any change makes a new instance.
public class ComponentMessage {

    private static final Component ROOT = Component.empty()
        .applyFallbackStyle(
            Style.style()
                .color(NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .build()
        );

    private final Component message;
    private final MessageType messageType;

    private ComponentMessage(@NotNull Component message, @NotNull MessageType messageType) {
        this.message = ROOT.append(message);
        this.messageType = messageType;
    }

    // Factories

    public static ComponentMessage componentMessage(@NotNull Component message, @NotNull MessageType messageType) {
        return new ComponentMessage(message, messageType);
    }

    public static ComponentMessage componentMessage(@NotNull Component message) {
        return componentMessage(message, MessageType.CHAT);
    }

    public static ComponentMessage componentMessage(@NotNull String message, @NotNull MessageType messageType) {
        return componentMessage(
            Utils.processString(message),
            messageType
        );
    }

    public static ComponentMessage componentMessage(@NotNull String message) {
        return componentMessage(message, MessageType.CHAT);
    }

    public static ComponentMessage componentMessage(@NotNull ConfigLoader<?> loader, @NotNull String path) {
        return Utils.getFromConfig(loader, path);
    }

    // Message Getters

    /**
     * Gets the underlying message.
     *
     * @return The underlying message.
     */
    public @NotNull Component get() {
        return message;
    }

    /**
     * Gets the underlying message as plain text.
     *
     * @return The underlying message as plain text.
     */
    public @NotNull String getAsPlainText() {
        return PlainTextComponentSerializer.plainText().serialize(message);
    }

    /**
     * Gets the underlying message as JSON.
     *
     * @return The underlying message as JSON.
     */
    public @NotNull String getAsJson() {
        return GsonComponentSerializer.gson().serialize(message);
    }

    /**
     * Gets the underlying message as Legacy text.
     *
     * @return The underlying message as Legacy text.
     */
    public @NotNull String getAsLegacy() {
        return LegacyComponentSerializer.legacySection().serialize(message);
    }

    /**
     * Gets the underlying message as MiniMessage text.
     *
     * @return The underlying message as MiniMessage text.
     */
    public @NotNull String getAsMiniMessage() {
        return MiniMessage.miniMessage().serialize(message);
    }

    // Class Methods

    /**
     * Gets the MessageType of this message.
     *
     * @return The MessageType of this message.
     */
    public @NotNull MessageType messageType() {
        return messageType;
    }

    /**
     * Sets the MessageType of this message.
     *
     * @param messageType The MessageType to set.
     * @return A new ComponentMessage with the updated MessageType.
     */
    public ComponentMessage messageType(@NotNull MessageType messageType) {
        return new ComponentMessage(message, messageType);
    }

    /**
     * Appends to the current message.
     *
     * @param append The object to append. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the appended content.
     */
    public ComponentMessage append(@NotNull Object append) {
        return new ComponentMessage(
            message.append(Utils.getComponentFromObject(append)),
            messageType
        );
    }

    /**
     * Prepends to the current message.
     *
     * @param prepend The object to prepend. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the prepended content.
     */
    public ComponentMessage prepend(@NotNull Object prepend) {
        return new ComponentMessage(
            Utils.getComponentFromObject(prepend),
            messageType
        ).append(message);
    }

    /**
     * Replaces all instances of the specified placeholder with the specified replacement.
     * @param placeholder The placeholder to replace.
     * @param replacement The replacement object. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the replacements made.
     */
    public ComponentMessage replace(@NotNull String placeholder, @Nullable Object replacement) {
        Replacer replacer = Replacer.replacer().addReplacement(placeholder, replacement);
        return new ComponentMessage(replacer.apply(message), messageType);
    }

    /**
     * Replaces all instances of the specified placeholders with the specified replacements.
     * @param replacements A map of placeholders to replacements. Explicitly supports {@link Component} and {@link ComponentMessage} as values. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the replacements made.
     */
    public ComponentMessage replace(@NotNull Map<String, Object> replacements) {
        Replacer replacer = Replacer.replacer().addReplacements(replacements);
        return new ComponentMessage(replacer.apply(message), messageType);
    }

    /**
     * Applies the specified Replacer to the message.
     * @param replacer The Replacer to apply.
     * @return A new ComponentMessage with the replacements made.
     */
    public ComponentMessage replace(@NotNull Replacer replacer) {
        return new ComponentMessage(replacer.apply(message), messageType);
    }

    /**
     * Parses PlaceholderAPI placeholders in the message for the specified player.
     * If PlaceholderAPI is not installed, the message is returned unchanged.
     *
     * @param player The player to parse placeholders for. Can be null for non-player specific placeholders.
     * @return A new ComponentMessage with the parsed placeholders.
     */
    public ComponentMessage parsePlaceholderAPI(@Nullable OfflinePlayer player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return this;
        }

        String stringMessage = MiniMessage.miniMessage().serialize(this.message);
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

        return new ComponentMessage(
            MiniMessage.miniMessage().deserialize(stringMessage),
            messageType
        );
    }

    /**
     * Checks if the underlying plain text matches the specified string.
     * @param string The string to check against.
     * @return True if the underlying plain text matches the specified string, false otherwise.
     */
    public boolean matchesString(@NotNull String string) {
        return getAsPlainText().equals(string);
    }

    /**
     * Checks if the underlying plain text contains the specified string.
     * @param string The string to check for.
     * @return True if the underlying plain text contains the specified string, false otherwise.
     */
    public boolean containsString(@NotNull String string) {
        return getAsPlainText().contains(string);
    }

    /**
     * Checks if the underlying plain text is empty.
     * @return True if the underlying plain text is empty, false otherwise.
     */
    public boolean isEmpty() {
        return getAsPlainText().isEmpty();
    }

    /**
     * Gets the length of the underlying plain text.
     * @return The length of the underlying plain text.
     */
    public int getLength() {
        return getAsPlainText().length();
    }

    // Sending

    /**
     * Sends the message to the specified Audience.
     *
     * @param audience The Audience to send the message to. If null, nothing happens.
     */
    public void send(@Nullable Audience audience) {
        messageType.send(audience, message);
    }

    /**
     * Sends the message to a list of Audiences.
     *
     * @param audienceList The list of Audiences to send the message to. If the list is empty, nothing happens.
     */
    public void send(@NotNull List<Audience> audienceList) {
        audienceList.forEach(this::send);
    }

    /**
     * Broadcasts the message to all players on the server.
     */
    public void broadcast() {
        Bukkit.broadcast(message);
    }

}
