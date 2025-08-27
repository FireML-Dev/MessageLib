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
import uk.firedev.messagelib.Utils;
import uk.firedev.messagelib.replacer.Replacer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

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
     * Appends to each line of the current message.
     *
     * @param append The object to append. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the content appended to each line.
     */
    public ComponentListMessage appendEachLine(@NotNull Object append) {
        Component resolved = Utils.getComponentFromObject(append);
        List<Component> newMessage = this.message.stream()
            .map(line -> new ComponentSingleMessage(line, messageType).append(resolved).get())
            .toList();
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
     * Prepends to each line of the current message.
     *
     * @param prepend The object to prepend. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the content prepended to each line.
     */
    public ComponentListMessage prependEachLine(@NotNull Object prepend) {
        Component resolved = Utils.getComponentFromObject(prepend);
        List<Component> newMessage = this.message.stream()
            .map(line -> new ComponentSingleMessage(line, messageType).prepend(resolved).get())
            .toList();
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
    public ComponentListMessage replace(@NotNull Map<String, ?> replacements) {
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

    /**
     * Converts this ComponentListMessage into a list of ComponentSingleMessages.
     *
     * @return A list of ComponentSingleMessages representing each line of the original message.
     */
    public List<ComponentSingleMessage> toSingleMessages() {
        return message.stream()
            .map(line -> new ComponentSingleMessage(line, this.messageType))
            .toList();
    }

    /**
     * Converts this ComponentListMessage into a single ComponentSingleMessage, joining all lines with newlines.
     *
     * @return A ComponentSingleMessage representing the original message.
     */
    public ComponentSingleMessage toSingleMessage() {
        return new ComponentSingleMessage(
            Component.join(JoinConfiguration.newlines(), this.message),
            this.messageType
        );
    }

    /**
     * Edits each line of the current message using the provided editor function.
     *
     * @param editor A function that takes a ComponentSingleMessage and returns a modified ComponentSingleMessage.
     * @return A new ComponentListMessage with each line edited.
     */
    public ComponentListMessage editAllLines(@NotNull Function<ComponentSingleMessage, ComponentSingleMessage> editor) {
        List<Component> newMessage = message.stream()
            .map(line -> editor.apply(new ComponentSingleMessage(line, this.messageType)))
            .filter(Objects::nonNull)
            .map(ComponentSingleMessage::get)
            .toList();
        return new ComponentListMessage(newMessage, this.messageType);
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
