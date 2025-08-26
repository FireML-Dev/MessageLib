package uk.firedev.messagelib.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MessageType {
    CHAT,
    ACTION_BAR,
    TITLE,
    SUBTITLE;

    public void send(@Nullable Audience audience, @NotNull Component message) {
        if (audience == null) {
            return;
        }
        switch (this) {
            case CHAT -> audience.sendMessage(message);
            case ACTION_BAR -> audience.sendActionBar(message);
            case TITLE -> {
                Title title = Title.title(message, Component.empty());
                audience.showTitle(title);
            }
            case SUBTITLE -> {
                Title title = Title.title(Component.empty(), message);
                audience.showTitle(title);
            }
        }
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
