package uk.firedev.messagelib.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.MessageLibSettings;
import uk.firedev.messagelib.ObjectProcessor;
import uk.firedev.messagelib.Utils;
import uk.firedev.messagelib.replacer.Replacer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

// NEEDS TO BE IMMUTABLE - any change makes a new instance.
public class ComponentSingleMessage extends ComponentMessage {

    private final Component message;
    private final MessageType messageType;
    private MiniMessage miniMessage = Utils.MINI_MESSAGE;

    protected ComponentSingleMessage(@NotNull Component message, @NotNull MessageType messageType) {
        this.message = ComponentMessage.ROOT.append(message);
        this.messageType = messageType;
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
     * {@inheritDoc}
     */
    @Override
    public @NotNull MiniMessage miniMessage() {
        return miniMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage miniMessage(@NotNull MiniMessage miniMessage) {
        ComponentSingleMessage newMessage = new ComponentSingleMessage(message, messageType);
        newMessage.miniMessage = miniMessage;
        return newMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage createCopy() {
        return new ComponentSingleMessage(this.message, messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull MessageType messageType() {
        return messageType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage messageType(@NotNull MessageType messageType) {
        return new ComponentSingleMessage(message, messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage append(@NotNull Object append) {
        if (!MessageLibSettings.get().isAllowEmptyAppend() && isEmpty()) {
            return this;
        }
        return new ComponentSingleMessage(
            message.append(Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(append))),
            messageType
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage prepend(@NotNull Object prepend) {
        if (!MessageLibSettings.get().isAllowEmptyPrepend() && isEmpty()) {
            return this;
        }
        Component processed = Component.join(JoinConfiguration.newlines(),ObjectProcessor.process(prepend));
        if (Utils.isEmpty(processed)) {
            return this;
        }
        return new ComponentSingleMessage(processed, messageType)
            .append(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage replace(@NotNull String placeholder, @Nullable Object replacement) {
        Replacer replacer = Replacer.replacer().addReplacement(placeholder, replacement);
        return new ComponentSingleMessage(replacer.apply(message), messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage replace(@NotNull Map<String, ?> replacements) {
        Replacer replacer = Replacer.replacer().addReplacements(replacements);
        return new ComponentSingleMessage(replacer.apply(message), messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage replace(@Nullable Replacer replacer) {
        if (replacer == null) {
            return this;
        }
        return new ComponentSingleMessage(replacer.apply(message), messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentSingleMessage parsePlaceholderAPI(@Nullable OfflinePlayer player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return this;
        }
        return new ComponentSingleMessage(
            Utils.parsePlaceholderAPI(message, player),
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
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return getAsPlainText().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {
        return getAsPlainText().length();
    }

    /**
     * Edits the underlying component using the provided editor function.
     * The editor function takes the current component as input and returns a modified component.
     *
     * @param editor A function that modifies the current component.
     * @return A new ComponentSingleMessage with the edited component.
     */
    public ComponentSingleMessage edit(@NotNull Function<Component, Component> editor) {
        return new ComponentSingleMessage(editor.apply(this.message), this.messageType);
    }

    // Sending

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@Nullable Audience audience) {
        if (isEmpty()) {
            return;
        }
        messageType.send(audience, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@NotNull List<? extends Audience> audienceList) {
        if (isEmpty()) {
            return;
        }
        audienceList.forEach(this::send);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@Nullable Audience... audiences) {
        if (isEmpty()) {
            return;
        }
        for (Audience audience : audiences) {
            send(audience);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void broadcast() {
        if (isEmpty()) {
            return;
        }
        Bukkit.broadcast(message);
    }

}
