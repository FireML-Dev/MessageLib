package uk.firedev.messagelib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.messagelib.message.ComponentListMessage;
import uk.firedev.messagelib.message.ComponentSingleMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ObjectProcessor {

    private static final List<Processor<?>> PROCESSORS = new ArrayList<>();

    static {
        registerProcessor(
            Component.class,
            component -> component
        );
        registerProcessor(
            ComponentSingleMessage.class,
            ComponentSingleMessage::get
        );
        registerProcessor(
            ComponentListMessage.class,
            listMessage -> Component.join(JoinConfiguration.newlines(), listMessage.get())
        );
    }

    /**
     * Processes an object into a Component using registered processors.
     * @param object The object to process.
     * @return The processed object.
     */
    public static @NotNull Component process(@NotNull Object object) {
        for (Processor<?> processor : PROCESSORS) {
            Component component = processor.process(object);
            if (component != null) {
                return component;
            }
        }

        // If no processor matches, toString the object.
        return Utils.processString(object.toString());
    }

    public static <T> void registerProcessor(@NotNull Class<T> clazz, @NotNull Function<T, Component> processor) {
        PROCESSORS.add(
            new Processor<>(clazz, processor)
        );
    }

    private static class Processor<T> {

        private final Class<T> clazz;
        private final Function<T, Component> processor;

        public Processor(@NotNull Class<T> clazz, @NotNull Function<T, Component> processor) {
            this.clazz = clazz;
            this.processor = processor;
        }

        public @Nullable Component process(@NotNull Object object) {
            if (clazz.isInstance(object)) {
                return processor.apply(clazz.cast(object));
            }
            return null;
        }

    }

}
