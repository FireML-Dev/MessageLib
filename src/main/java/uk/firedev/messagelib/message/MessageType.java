package uk.firedev.messagelib.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public enum MessageType {
    CHAT(Audience::sendMessage),
    ACTION_BAR(Audience::sendActionBar),
    TITLE((audience, component) -> audience.sendTitlePart(TitlePart.TITLE, component)),
    SUBTITLE((audience, component) -> audience.sendTitlePart(TitlePart.SUBTITLE, component));

    private final BiConsumer<Audience, Component> consumer;

    MessageType(@NotNull BiConsumer<Audience, Component> consumer) {
        this.consumer = consumer;
    }

    public void send(@Nullable Audience audience, @NotNull Component message) {
        if (audience == null) {
            return;
        }
        consumer.accept(audience, message);
    }

    /**
     * Get a MessageType from a string, defaults to CHAT if the string is null or invalid.
     * @param type The string to convert to a MessageType.
     * @return The MessageType, or CHAT if the string is null or invalid.
     */
    public static @NotNull MessageType getFromString(@Nullable String type) {
        if (type == null) {
            return CHAT;
        }
        try {
            return MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CHAT;
        }
    }

}
