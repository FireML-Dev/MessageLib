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
import uk.firedev.messagelib.ObjectProcessor;
import uk.firedev.messagelib.Utils;
import uk.firedev.messagelib.replacer.Replacer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

// NEEDS TO BE IMMUTABLE - any change makes a new instance.
public class ComponentListMessage extends ComponentMessage {

    private final List<Component> message = new ArrayList<>();
    private final MessageType messageType;
    private MiniMessage miniMessage = Utils.MINI_MESSAGE;

    protected ComponentListMessage(@NotNull List<Component> message, @NotNull MessageType messageType) {
        message.stream().map(ROOT::append).forEach(this.message::add);
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
        return new ArrayList<>(message);
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
            .map(component -> miniMessage.serialize(component))
            .toList();
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
    public ComponentListMessage miniMessage(@NotNull MiniMessage miniMessage) {
        ComponentListMessage newMessage = new ComponentListMessage(message, messageType);
        newMessage.miniMessage = miniMessage;
        return newMessage;
    }

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
        newMessage.addAll(
            ObjectProcessor.process(append).stream()
                .map(ROOT::append)
                .toList()
        );
        return new ComponentListMessage(newMessage, messageType);
    }

    /**
     * Appends to each line of the current message.
     *
     * @param append The object to append. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the content appended to each line.
     */
    public ComponentListMessage appendEachLine(@NotNull Object append) {
        Component resolved = Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(append));
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
        List<Component> newMessage = new ArrayList<>(
            ObjectProcessor.process(prepend).stream()
                .map(ROOT::append)
                .toList()
        );
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
        Component resolved = Component.join(JoinConfiguration.newlines(), ObjectProcessor.process(prepend));
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
     * Replaces all instances of the specified placeholder with the specified replacement
     * <p>
     * If a placeholder is found on any line, the replacement list will be inserted in its place.
     * @param placeholder The placeholder to replace.
     * @param replacement The replacement object. Explicitly supports {@link Component} and {@link ComponentMessage}. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the replacements made.
     */
    public ComponentListMessage replaceWithListInsertion(@NotNull String placeholder, @Nullable Object replacement) {
        Replacer replacer = Replacer.replacer().addReplacement(placeholder, replacement);
        return new ComponentListMessage(replacer.applyWithListInsertion(message), messageType);
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
     * Replaces all instances of the specified placeholders with the specified replacements.
     * <p>
     * If a placeholder is found on any line, the replacement list will be inserted in its place.
     * @param replacements A map of placeholders to replacements. Explicitly supports {@link Component} and {@link ComponentSingleMessage} as values. Anything else will be converted to a String and processed.
     * @return A new ComponentMessage with the replacements made.
     */
    public ComponentListMessage replaceWithListInsertion(@NotNull Map<String, ?> replacements) {
        Replacer replacer = Replacer.replacer().addReplacements(replacements);
        return new ComponentListMessage(replacer.applyWithListInsertion(message), messageType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentListMessage replace(@Nullable Replacer replacer) {
        if (replacer == null) {
            return this;
        }
        return new ComponentListMessage(replacer.apply(message), messageType);
    }

    /**
     * Applies the specified Replacer to the message.
     * <p>
     * If a placeholder is found on any line, the replacement list will be inserted in its place.
     * @param replacer The Replacer to apply.
     * @return A new ComponentMessage with the replacements made.
     */
    public ComponentListMessage replaceWithListInsertion(@Nullable Replacer replacer) {
        if (replacer == null) {
            return this;
        }
        return new ComponentListMessage(replacer.applyWithListInsertion(message), messageType);
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
     * Edits the current message using the provided editor function.
     *
     * @param editor A function that takes a List of Components and returns a modified List of Components.
     * @return A new ComponentListMessage with the edited message.
     */
    public ComponentListMessage edit(@NotNull Function<List<Component>, List<Component>> editor) {
        List<Component> newMessage = editor.apply(new ArrayList<>(this.message));
        return new ComponentListMessage(newMessage, this.messageType);
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
        if (isEmpty()) {
            return;
        }
        message.forEach(component -> messageType.send(audience, component));
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
        message.forEach(Bukkit::broadcast);
    }

}
