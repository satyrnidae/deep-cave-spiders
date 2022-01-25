package dev.satyrn.deepcavespiders.configuration;

import dev.satyrn.deepcavespiders.DeepCaveSpiders;
import dev.satyrn.deepcavespiders.util.SpawnDistribution;
import dev.satyrn.papermc.api.configuration.v1.*;
import dev.satyrn.papermc.api.configuration.v2.EnumListNode;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Level;

/**
 * Represents the configuration for the plugin.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public final class Configuration extends ConfigurationContainer {
    /**
     * The locale to use for internationalization.
     */
    public final transient StringNode locale = new StringNode(this, "locale");

    /**
     * The spawning options.
     */
    public final transient SpawnOptionsContainer spawnOptions = new SpawnOptionsContainer(this);

    /**
     * List of biomes in which cave spiders should be allowed to spawn.
     */
    public final transient EnumListNode<Biome> biomes = new EnumListNode<>(this, "biomes") {
        @Override
        protected @NotNull Biome parse(@NotNull String value) throws IllegalArgumentException {
            return Biome.valueOf(value.toUpperCase(Locale.ROOT));
        }
    };

    /**
     * List of environments in which cave spiders should be allowed to spawn.
     */
    public final transient EnumListNode<World.Environment> environments = new EnumListNode<>(this, "environments") {
        @Override
        protected @NotNull World.Environment parse(@NotNull String value) throws IllegalArgumentException {
            return World.Environment.valueOf(value.toUpperCase(Locale.ROOT));
        }
    };

    /**
     * List of entity types which may be replaced by cave spiders.
     */
    public final EnumListNode<EntityType> replaceEntities = new EnumListNode<EntityType>(this, "replaceEntities") {
        @Override
        protected @NotNull EntityType parse(@NotNull String value) throws IllegalArgumentException {
            return EntityType.valueOf(value.toUpperCase(Locale.ROOT));

        }
    };

    /**
     * Whether to enable debug logging.
     */
    public final @NotNull BooleanNode debug = new BooleanNode(this, "debug");

    /**
     * Whether to enable plugin metrics.
     */
    public final @NotNull BooleanNode metrics = new BooleanNode(this, "metrics");


    /**
     * Creates a new instance of the plugin configuration.
     *
     * @param plugin The plugin.
     */
    public Configuration(final Plugin plugin) {
        super(plugin);
    }

    /**
     * Container with spawning options.
     *
     * @author Isabel Maskrey
     * @since 1.0-SNAPSHOT
     */
    public static class SpawnOptionsContainer extends ConfigurationContainer {
        /**
         * Spawn range configuration options.
         */
        public final transient SpawnRangeContainer range = new SpawnRangeContainer(this);

        /**
         * Controls the function which determines the percentage of spiders spawning at a given depth.
         */
        public final transient EnumNode<SpawnDistribution> distribution = new EnumNode<>(this, "distribution") {
            @Override
            protected @NotNull SpawnDistribution parse(@NotNull String value) throws IllegalArgumentException {
                return SpawnDistribution.valueOf(value.toUpperCase());
            }

            @Override
            protected @NotNull SpawnDistribution getDefault() {
                return SpawnDistribution.CONSTANT;
            }
        };

        /**
         * Spawn chances configuration options.
         */
        public final transient SpawnChancesContainer chances = new SpawnChancesContainer(this);

        /**
         * Chance that a cave spider will spawn a jockey.
         */
        public final transient DoubleNode jockeyChance = new DoubleNode(this, "jockeyChance", 0D, 1D);

        /**
         * Creates a new instance of the spawn options container.
         *
         * @param parent The parent configuration container.
         */
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
        /**
         * The minimum Y value to spawn at.
         */
        public final transient IntegerNode minY = new IntegerNode(this, "minY", -64, 320) {
            @Override
            public @NotNull Integer defaultValue() {
                return -64;
            }
        };

        /**
         * The maximum Y value to spawn at.
         */
        public final transient IntegerNode maxY = new IntegerNode(this, "maxY", -64, 320) {
            @Override
            public @NotNull Integer defaultValue() {
                return -8;
            }
        };

        /**
         * Allows spiders to spawn below the minimum Y value.
         * This is useful for customizing the ramp function; spawns below the minimum Y value will always use the
         * maximum spawn chance set in the configuration file.
         */
        public final transient BooleanNode allowSpawnsBelowMinY = new BooleanNode(this, "allowSpawnsBelowMinY");

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
        /**
         * The easy difficulty spawn chances
         */
        private final transient DoubleNode easy = new DoubleNode(this, "easy", 0D, 1D) {
            @Override
            public @NotNull Double defaultValue() {
                return 0.05D;
            }
        };

        /**
         * The normal difficulty spawn chances
         */
        private final transient DoubleNode normal = new DoubleNode(this, "normal", 0D, 1D) {
            @Override
            public @NotNull Double defaultValue() {
                return 0.1D;
            }
        };

        /**
         * The hard difficulty spawn chances
         */
        private final transient DoubleNode hard = new DoubleNode(this, "hard", 0D, 1D) {
            @Override
            public @NotNull Double defaultValue() {
                return 0.5D;
            }
        };

        /**
         * Creates a new spawn chances container.
         *
         * @param parent The container's parent.
         */
        SpawnChancesContainer(ConfigurationContainer parent) {
            super(parent, "chances");
        }

        /**
         * Gets the value for a given world difficulty.
         *
         * @param difficulty The world difficulty.
         * @return The value for the given difficulty.
         */
        public double value(final @NotNull Difficulty difficulty) {
            return difficulty == Difficulty.HARD ? this.hard.value() : difficulty == Difficulty.NORMAL ? this.normal.value() : difficulty == Difficulty.EASY ? this.easy.value() : 0D;
        }
    }
}
