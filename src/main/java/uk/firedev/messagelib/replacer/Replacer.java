package uk.firedev.messagelib.replacer;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.Utils;
import uk.firedev.messagelib.message.ComponentSingleMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Replacer {

    private final Map<String, Object> replacements = new HashMap<>();

    private Replacer() {}

    /**
     * Creates a new Replacer instance.
     * @return The new Replacer instance.
     */
    public static Replacer replacer() {
        return new Replacer();
    }

    /**
     * Adds a single replacement to this Replacer.
     * @param string The string to replace.
     * @param replacement The replacement object. Explicitly supports {@link Component} and {@link ComponentSingleMessage}. Anything else will be converted to a String and processed.
     * @return The modified Replacer.
     */
    public Replacer addReplacement(@NotNull String string, @Nullable Object replacement) {
        this.replacements.put(string, replacement);
        return this;
    }

    /**
     * Adds a map of replacements to this Replacer.
     * @param replacements The replacements to add. Explicitly supports {@link Component} and {@link ComponentSingleMessage} as values. Anything else will be converted to a String and processed.
     * @return The modified Replacer
     */
    public Replacer addReplacements(@NotNull Map<String, Object> replacements) {
        this.replacements.putAll(replacements);
        return this;
    }

    /**
     * Merges other Replacers into this one.
     * @param others The other Replacers to merge.
     * @return The modified Replacer.
     */
    public Replacer merge(@NotNull Replacer... others) {
        for (Replacer other : others) {
            if (other == this) {
                continue;
            }
            replacements.putAll(other.replacements);
        }
        return this;
    }

    /**
     * Clears all replacements from this Replacer.
     * @return The modified Replacer.
     */
    public Replacer clear() {
        this.replacements.clear();
        return this;
    }

    /**
     * Applies the replacements to the provided Component.
     * @param component The component to apply the replacements to.
     * @return The modified component.
     */
    public Component apply(@NotNull Component component) {
        for (Map.Entry<String, Object> entry : replacements.entrySet()) {
            component = component.replaceText(
                builder -> builder.matchLiteral(entry.getKey()).replacement(Utils.getComponentFromObject(entry.getValue()))
            );
        }
        return component;
    }

    /**
     * Applies the replacements to a list of Components.
     * @param components The list of components to apply the replacements to.
     * @return The modified list of components.
     */
    public List<Component> apply(@NotNull List<Component> components) {
        return components.stream().map(this::apply).toList();
    }

}
