package uk.firedev.messagelib;

import org.jetbrains.annotations.NotNull;

public class MessageLibSettings {

    private static final MessageLibSettings instance = new MessageLibSettings();

    private boolean enableLegacy = false;
    private boolean allowEmptyAppend = false;
    private boolean allowEmptyPrepend = false;

    private MessageLibSettings() {}

    public static @NotNull MessageLibSettings get() {
        return instance;
    }

    public boolean isEnableLegacy() {
        return this.enableLegacy;
    }

    public void setEnableLegacy(boolean allow) {
        this.enableLegacy = allow;
    }

    public boolean isAllowEmptyAppend() {
        return this.allowEmptyAppend;
    }

    public void setAllowEmptyAppend(boolean allowEmptyAppend) {
        this.allowEmptyAppend = allowEmptyAppend;
    }

    public boolean isAllowEmptyPrepend() {
        return this.allowEmptyPrepend;
    }

    public void setAllowEmptyPrepend(boolean allowEmptyPrepend) {
        this.allowEmptyPrepend = allowEmptyPrepend;
    }

}
