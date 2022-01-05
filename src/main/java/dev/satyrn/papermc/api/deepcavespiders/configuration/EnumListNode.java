package dev.satyrn.papermc.api.deepcavespiders.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a configuration node with a list of enum values.
 *
 * @param <E> The enum type.
 * @author Isabel Maskrey
 * @since v2
 */
public abstract class EnumListNode<E extends Enum<E>> extends ConfigurationNode<List<E>> {

    /**
     * Creates a new configuration node with a list of enum values.
     *
     * @param parent The parent configuration.
     * @param name   The node's name.
     */
    public EnumListNode(final @NotNull ConfigurationContainer parent, final @NotNull String name) {
        super(parent, name, parent.config);
    }

    /**
     * Gets the value of the node.
     *
     * @return The value.
     */
    @Override
    public @NotNull List<E> value() {
        final @NotNull List<E> list = new ArrayList<>();
        final @NotNull List<String> values = this.config.getStringList(this.getPath());
        for (final String value : values) {
            if (value != null && !value.isEmpty()) {
                try {
                    final E parsedValue = this.parse(value);
                    list.add(parsedValue);
                } catch (IllegalArgumentException ex) {
                    // Do nothing
                }
            }
        }
        return list;
    }

    /**
     * Parses the enum value.
     *
     * @param value The string value from the config file
     * @return The parsed enum value.
     * @throws IllegalArgumentException Thrown when an invalid value is parsed.
     */
    protected abstract @NotNull E parse(final @NotNull String value) throws IllegalArgumentException;
}
