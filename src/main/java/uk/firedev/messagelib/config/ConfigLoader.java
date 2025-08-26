package uk.firedev.messagelib.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ConfigLoader<T> {

    @Nullable Object getObject(String path);

    @Nullable String getString(String path);

    @NotNull List<String> getStringList(String path);

    @NotNull T getConfig();

    @Nullable ConfigLoader<T> getSection(@NotNull String path);

}
