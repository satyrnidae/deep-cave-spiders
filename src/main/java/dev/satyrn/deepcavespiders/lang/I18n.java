package dev.satyrn.deepcavespiders.lang;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Message internationalization for the plugin
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public final class I18n extends dev.satyrn.papermc.api.lang.v1.I18n {
    /**
     * The base name for all locales without a language name.
     */
    private static final String BASE_NAME = "deepcavespiders";
    /**
     * The internationalization instance.
     */
    private static I18n instance;

    /**
     * Initializes a new I18n instance.
     *
     * @param plugin The parent plugin instance.
     */
    public I18n(final Plugin plugin) {
        super(plugin, ResourceBundle.getBundle(BASE_NAME, DEFAULT_LOCALE, new Utf8LangFileControl()));
    }

    /**
     * Translates a resource string to the current locale.
     *
     * @param key    The translation key.
     * @param format The translation format.
     * @return The translated message.
     */
    @NotNull
    public static String tr(@NotNull final String key, @NotNull final Object... format) {
        return instance.translate(key, format);
    }

    /**
     * Gets a resource bundle for the given locale.
     * @param locale The current locale
     * @return The resource bundle for the current locale.
     */
    @Override
    protected ResourceBundle getResourceBundleForLocale(@NotNull Locale locale) {
        return ResourceBundle.getBundle(BASE_NAME, locale, new Utf8LangFileControl());
    }

    /**
     * Enables the i18n handler.
     */
    public void enable() {
        instance = this;
    }

    /**
     * Disables the i18n handler.
     */
    public void disable() {
        instance = null;
    }
}
