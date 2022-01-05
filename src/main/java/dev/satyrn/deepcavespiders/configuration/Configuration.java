package dev.satyrn.deepcavespiders.configuration;

import dev.satyrn.papermc.api.deepcavespiders.configuration.*;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * Represents the configuration for the plugin.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public final class Configuration extends ConfigurationContainer {
    // The locale to use for internationalization.
    private final transient StringNode locale = new StringNode(this, "locale");
    // Spawn options container.
    private final transient SpawnOptionsContainer spawnOptions = new SpawnOptionsContainer(this);
    // List of biomes that cave spiders will be allowed to spawn in.
    private final transient EnumListNode<Biome> biomes = new EnumListNode<>(this, "biomes") {
        @Override
        protected @NotNull Biome parse(@NotNull String value) throws IllegalArgumentException {
            return Biome.valueOf(value.toUpperCase(Locale.ROOT));
        }
    };
    // List of environments that cave spiders will be allowed to spawn in.
    private final transient EnumListNode<World.Environment> environments = new EnumListNode<>(this, "environments") {
        @Override
        protected @NotNull World.Environment parse(@NotNull String value) throws IllegalArgumentException {
            return World.Environment.valueOf(value.toUpperCase(Locale.ROOT));
        }
    };

    /**
     * Creates a new instance of the plugin configuration.
     *
     * @param plugin The plugin.
     */
    public Configuration(final Plugin plugin) {
        super(plugin.getConfig());
    }

    /**
     * Gets the maximum spawn height.
     *
     * @return The maximum spawn height.
     */
    public int getMaxY() {
        return this.spawnOptions.range.maxY.value();
    }

    /**
     * Gets the minimum spawn height.
     *
     * @return The minimum spawn height.
     */
    public int getMinY() {
        return this.spawnOptions.range.minY.value();
    }

    /**
     * Gets the spawn chance.
     *
     * @param difficulty The world difficulty.
     * @return The spawn chance.
     */
    public double getSpawnChance(Difficulty difficulty) {
        switch(difficulty) {
            case EASY -> {
                return this.spawnOptions.chances.easy.value();
            }
            case NORMAL -> {
                return this.spawnOptions.chances.normal.value();
            }
            case HARD -> {
                return this.spawnOptions.chances.hard.value();
            }
        }
        return 0D;
    }

    /**
     * Gets the rider chance.
     *
     * @return The rider chance.
     */
    public double getJockeyChance() {
        return this.spawnOptions.jockeyChance.value();
    }

    /**
     * Gets the biome whitelist.
     *
     * @return The biome whitelist.
     */
    public @NotNull List<Biome> getBiomes() {
        return this.biomes.value();
    }

    /**
     * Gets the environment whitelist.
     *
     * @return The environment whitelist.
     */
    public @NotNull List<World.Environment> getEnvironments() {
        return this.environments.value();
    }

    /**
     * Gets the current locale.
     *
     * @return The locale.
     */
    public @NotNull String getLocale() {
        final String locale = this.locale.value();
        return locale == null ? "en_US" : locale;
    }

    /**
     * Container with spawning options.
     *
     * @author Isabel Maskrey
     * @since 1.0-SNAPSHOT
     */
    public static class SpawnOptionsContainer extends ConfigurationContainer {
        // Configuration container with spawn height ranges.
        final transient SpawnRangeContainer range = new SpawnRangeContainer(this);
        // Configuration container with spawn chances.
        final transient SpawnChancesContainer chances = new SpawnChancesContainer(this);
        // Chances that a cave spider will spawn with a baby zombie jockey.
        private final transient DoubleNode jockeyChance = new DoubleNode(this, "jockeyChance");

        SpawnOptionsContainer(final ConfigurationContainer parent) {
            super(parent, "spawnOptions");
        }
    }

    /**
     * Container with spawning height range options.
     *
     * @author Isabel Maskrey
     * @since 1.0-SNAPSHOT
     */
    public static class SpawnRangeContainer extends ConfigurationContainer {
        // The minimum Y height to spawn at.
        final transient IntegerNode minY = new IntegerNode(this, "minY");
        // The maximum Y height to spawn at.
        final transient IntegerNode maxY = new IntegerNode(this, "maxY");

        /**
         * Creates a new spawn range configuration container.
         *
         * @param parent The container's parent.
         */
        SpawnRangeContainer(ConfigurationContainer parent) {
            super(parent, "range");
        }
    }

    /**
     * Container with difficulty-based spawning chances.
     *
     * @author Isabel Maskrey
     * @since 1.0-SNAPSHOT
     */
    public static class SpawnChancesContainer extends ConfigurationContainer {
        // The easy difficulty spawn chances
        final transient DoubleNode easy = new DoubleNode(this, "easy");
        // The normal difficulty spawn chances
        final transient DoubleNode normal = new DoubleNode(this, "normal");
        // The hard difficulty spawn chances
        final transient DoubleNode hard = new DoubleNode(this, "hard");

        /**
         * Creates a new spawn chances container.
         *
         * @param parent The container's parent.
         */
        SpawnChancesContainer(ConfigurationContainer parent) {
            super(parent, "chances");
        }
    }
}
