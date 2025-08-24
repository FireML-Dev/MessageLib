package uk.firedev.plugintemplate;

import org.bukkit.plugin.java.JavaPlugin;

public final class PluginTemplate extends JavaPlugin {

    private static PluginTemplate INSTANCE;

    public PluginTemplate() {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException(getClass().getName() + " has already been assigned!");
        }
        INSTANCE = this;
    }

    public static @NotNull PluginTemplate getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException(PluginTemplate.class.getSimpleName() + " has not been assigned!");
        }
        return INSTANCE;
    }

    @Override
    public void onLoad() {}

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

}
