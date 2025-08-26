package uk.firedev.messagelib.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.Utils;
import uk.firedev.messagelib.replacer.Replacer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// NEEDS TO BE IMMUTABLE - any change makes a new instance.
public class ComponentListMessage extends ComponentMessage {

    private final List<Component> message = new ArrayList<>();
    private final MessageType messageType;

    protected ComponentListMessage(@NotNull List<Component> message, @NotNull MessageType messageType) {
        this.message.addAll(message);
        this.messageType = messageType;
    }

    protected ComponentListMessage(@NotNull Component message, @NotNull MessageType messageType) {
        this.message.add(ComponentMessage.ROOT.append(message));
        this.messageType = messageType;
    }

    // Message Getters

    /**
     * Gets the underlying message.
     *
     * @return The underlying message.
     */
    public @NotNull List<Component> get() {
        return message;
    }

    /**
     * Gets the underlying message as plain text.
     *
     * @return The underlying message as plain text.
     */
    public @NotNull List<String> getAsPlainText() {
        return message.stream()
            .map(component -> PlainTextComponentSerializer.plainText().serialize(component))
            .toList();
    }

    /**
     * Gets the underlying message as JSON.
     *
     * @return The underlying message as JSON.
     */
    public @NotNull List<String> getAsJson() {
        return message.stream()
            .map(component -> GsonComponentSerializer.gson().serialize(component))
            .toList();
    }

    /**
     * Gets the underlying message as Legacy text.
     *
     * @return The underlying message as Legacy text.
     */
    public @NotNull List<String> getAsLegacy() {
        return message.stream()
            .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
            .toList();
    }

    /**
     * Gets the underlying message as MiniMessage text.
     *
     * @return The underlying message as MiniMessage text.
     */
    public @NotNull List<String> getAsMiniMessage() {
        return message.stream()
            .map(component -> MiniMessage.miniMessage().serialize(component))
            .toList();
    }

    // Class Methods

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentListMessage createCopy() {
        return new ComponentListMessage(
            List.copyOf(this.message),
            messageType
        );
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
    public ComponentListMessage messageType(@NotNull MessageType messageType) {
        return new ComponentListMessage(message, messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentListMessage append(@NotNull Object append) {
        List<Component> newMessage = new ArrayList<>(message);
        if (append instanceof ComponentListMessage listMessage) {
            newMessage.addAll(listMessage.get());
        } else {
            newMessage.add(ComponentMessage.ROOT.append(Utils.getComponentFromObject(append)));
        }
        return new ComponentListMessage(newMessage, messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentListMessage prepend(@NotNull Object prepend) {
        List<Component> newMessage = new ArrayList<>();
        if (prepend instanceof ComponentListMessage listMessage) {
            newMessage.addAll(listMessage.get());
        } else {
            newMessage.add(ComponentMessage.ROOT.append(Utils.getComponentFromObject(prepend)));
        }
        newMessage.addAll(message);
        return new ComponentListMessage(newMessage, messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentListMessage replace(@NotNull String placeholder, @Nullable Object replacement) {
        Replacer replacer = Replacer.replacer().addReplacement(placeholder, replacement);
        return new ComponentListMessage(replacer.apply(message), messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentListMessage replace(@NotNull Map<String, Object> replacements) {
        Replacer replacer = Replacer.replacer().addReplacements(replacements);
        return new ComponentListMessage(replacer.apply(message), messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentListMessage replace(@NotNull Replacer replacer) {
        return new ComponentListMessage(replacer.apply(message), messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentListMessage parsePlaceholderAPI(@Nullable OfflinePlayer player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return this;
        }

        return new ComponentListMessage(
            message.stream()
                .map(component -> Utils.parsePlaceholderAPI(component, player))
                .toList(),
            messageType
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return message.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {
        return message.size();
    }

    // Sending

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@Nullable Audience audience) {
        message.forEach(component -> messageType.send(audience, component));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@NotNull List<Audience> audienceList) {
        audienceList.forEach(this::send);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void broadcast() {
        message.forEach(Bukkit::broadcast);
    }

}
