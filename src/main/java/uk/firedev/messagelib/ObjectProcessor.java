package uk.firedev.messagelib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.message.ComponentListMessage;
import uk.firedev.messagelib.message.ComponentSingleMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A class for processing different classes into Components.
 * <p>
 * This returns a list to be compatible with list messages.
 */
public class ObjectProcessor {

    private static final List<Processor<?>> PROCESSORS = new ArrayList<>();

    static {
        registerProcessor(
            Component.class,
            List::of
        );
        registerProcessor(
            ComponentSingleMessage.class,
            singleMessage -> List.of(singleMessage.get())
        );
        registerProcessor(
            ComponentListMessage.class,
            ComponentListMessage::get
        );
    }

    /**
     * Processes an object into a Component list using registered processors.
     * @param object The object to process.
     * @return The processed object.
     */
    public static @NotNull List<Component> process(@Nullable Object object) {
        return process(object, Utils.MINI_MESSAGE);
    }

    /**
     * Processes an object into a Component list using registered processors.
     * @param object The object to process.
     * @param miniMessage The MiniMessage instance to use for deserialization.
     * @return The processed object.
     */
    public static @NotNull List<Component> process(@Nullable Object object, @NotNull MiniMessage miniMessage) {
        if (object == null) {
            return List.of();
        }
        // Process every object in a list individually, otherwise the list will become a single String.
        if (object instanceof List<?> list) {
            List<Component> processed = new ArrayList<>();
            for (Object obj : list) {
                processed.addAll(process(obj, miniMessage));
            }
            return processed;
        }
        for (Processor<?> processor : PROCESSORS) {
            // We cannot pass the MiniMessage instance here.
            List<Component> components = processor.process(object);
            if (components != null) {
                return components;
            }
        }

        // If no processor matches, #toString the object.
        return List.of(Utils.processString(object.toString(), miniMessage));
    }

    /**
     * Registers a processor for the provided class.
     * @param clazz The class to process.
     * @param processor The component provider.
     */
    public static <T> void registerProcessor(@NotNull Class<T> clazz, @NotNull Function<T, List<Component>> processor) {
        PROCESSORS.add(
            new Processor<>(clazz, processor)
        );
    }

    private static class Processor<T> {

        private final Class<T> clazz;
        private final Function<T, List<Component>> processor;

        public Processor(@NotNull Class<T> clazz, @NotNull Function<T, List<Component>> processor) {
            this.clazz = clazz;
            this.processor = processor;
        }

        public @Nullable List<Component> process(@NotNull Object object) {
            if (clazz.isInstance(object)) {
                return processor.apply(clazz.cast(object));
            }
            return null;
        }

    }

}
