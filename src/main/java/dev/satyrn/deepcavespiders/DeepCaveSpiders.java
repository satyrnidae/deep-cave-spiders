package dev.satyrn.deepcavespiders;

import dev.satyrn.deepcavespiders.configuration.Configuration;
import dev.satyrn.deepcavespiders.listeners.SpawnEntityListener;
import dev.satyrn.papermc.api.lang.v1.I18n;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Represents the Deep Cave Spiders plugin.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
@SuppressWarnings("unused")
public final class DeepCaveSpiders extends JavaPlugin {
    // Event listener for entity spawn events.
    private SpawnEntityListener spawnEntityListener;
    // Internationalization instance.
    private I18n i18n;
    // The configuration instance.
    private Configuration configuration;

    /**
     * Called when the plugin is enabled.
     *
     * @since 1.0-SNAPSHOT
     */
    @Override
    public void onEnable() {
        this.configuration = new Configuration(this);
        if (this.configuration.debug.value()) {
            this.getLogger().setLevel(Level.ALL);
        }

        this.i18n = new I18n(this, "lang");
        this.i18n.setLocale(configuration.locale.value());
        i18n.enable();

        this.registerEvents(this.configuration);

        this.registerCommands();

        if (configuration.metrics.value()) {
            final Metrics metrics = new Metrics(this, 14026);
            metrics.addCustomChart(new SimplePie("distribution_method", () -> configuration.spawnOptions.distribution.value().toString().toLowerCase(Locale.ROOT)));
            metrics.addCustomChart(new SimplePie("locale", configuration.locale::value));
            metrics.addCustomChart(new AdvancedPie("biomes", () -> {
                final Map<String, Integer> values = new HashMap<>();
                for (final Biome biome : configuration.biomes.value()) {
                    final @NotNull String biomeName = biome.toString().toLowerCase(Locale.ROOT);
                    if (!values.containsKey(biomeName)) {
                        values.put(biomeName, 1);
                    }
                }
                return values;
            }));
            metrics.addCustomChart(new AdvancedPie("environments", () -> {
                final Map<String, Integer> values = new HashMap<>();
                for (final World.Environment environment : configuration.environments.value()) {
                    final @NotNull String environmentName = environment.toString().toLowerCase(Locale.ROOT);
                    if (!values.containsKey(environmentName)) {
                        values.put(environmentName, 1);
                    }
                }
                return values;
            }));
            metrics.addCustomChart(new AdvancedPie("replaced_entities", () -> {
                final Map<String, Integer> values = new HashMap<>();
                for (final EntityType replacedEntity : configuration.replaceEntities.value()) {
                    final @NotNull String entityName = replacedEntity.toString().toLowerCase(Locale.ROOT);
                    if (!values.containsKey(entityName)) {
                        values.put(entityName, 1);
                    }
                }
                return values;
            }));
        }

        this.getLogger().info("DeepCaveSpiders has been successfully enabled!");
    }

    /**
     * Called when the plugin is loaded.
     *
     * @since 1.0-SNAPSHOT
     */
    @Override
    public void onLoad() {
        this.getLogger().info("Initializing DeepCaveSpiders...");
        this.saveDefaultConfig();
    }

    /**
     * Requests a list of possible completion options for a command.
     *
     * @param sender The command sender.
     * @param command The command.
     * @param alias The command alias.
     * @param args The command arguments.
     * @return A list of all possible completions for the current argument.
     * @since 1.0-SNAPSHOT
     */
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completionOptions = new ArrayList<>();
        if ("deepcavespiders".equalsIgnoreCase(command.getName())) {
            if (args.length == 1) {
                if (sender.hasPermission("deepcavespiders.admin")) {
                    completionOptions.add("reload");
                }
            }
        }
        return completionOptions;
    }

    /**
     * Executes the given command, returning whether it was successful.
     *
     * @param sender The command sender
     * @param command The command to execute.
     * @param label The command alias
     * @param args The command arguments.
     * @return {@code true} if the command was executed successfully; otherwise, {@code false}
     * @since 1.0-SNAPSHOT
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if ("deepcavespiders".equalsIgnoreCase(command.getName())) {
            if (args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
                if (sender.hasPermission("deepcavespiders.admin")) {
                    this.reloadConfig();
                    final Configuration configuration = new Configuration(this);
                    sender.sendMessage(I18n.tr("command.reload"));
                } else {
                    sender.sendMessage(I18n.tr("command.reload.deny"));
                }
            } else {
                final int minY = this.configuration.spawnOptions.range.minY.value();
                final int maxY = this.configuration.spawnOptions.range.maxY.value();
                final boolean allowSpawnsBelowMinY = this.configuration.spawnOptions.range.allowSpawnsBelowMinY.value();
                sender.sendMessage(I18n.tr("command.about",
                        this.getDescription().getName(),
                        this.getDescription().getVersion(),
                        String.join(", ", this.getDescription().getAuthors()),
                        maxY,
                        (allowSpawnsBelowMinY ? "<" : "") + minY,
                        this.configuration.spawnOptions.chances.value(Difficulty.EASY),
                        this.configuration.spawnOptions.chances.value(Difficulty.NORMAL),
                        this.configuration.spawnOptions.chances.value(Difficulty.HARD),
                        this.configuration.spawnOptions.distribution.value().toString().toLowerCase(Locale.ROOT)));
                if (sender instanceof final Player player) {
                    double y = player.getLocation().getY();
                    double spawnChance = this.spawnEntityListener.getSpawnChance(player.getWorld().getDifficulty(), y) * 100;
                    if (y > maxY) spawnChance = 0;
                    if (y < minY && !allowSpawnsBelowMinY) spawnChance = 0;
                    final DecimalFormat decimalFormat = new DecimalFormat("0.0#");

                    sender.sendMessage(I18n.tr("command.about.spawnChanceAtCurrentY",
                            decimalFormat.format(player.getLocation().getY()),
                            decimalFormat.format(spawnChance) + "%"));
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Registers event listeners.
     *
     * @since 1.0-SNAPSHOT
     * @param configuration The configuration instance.
     */
    private void registerEvents(Configuration configuration) {
        if (this.spawnEntityListener == null) {
            this.spawnEntityListener = new SpawnEntityListener(this, configuration);
            final PluginManager pluginManager = this.getServer().getPluginManager();
            pluginManager.registerEvents(this.spawnEntityListener, this);
        }
    }

    /**
     * Registers commands.
     *
     * @since 1.0-SNAPSHOT
     */
    private void registerCommands() {
        final PluginCommand deepCaveSpiders = this.getServer().getPluginCommand("deepcavespiders");
        if (deepCaveSpiders != null) {
            deepCaveSpiders.setExecutor(this);
            deepCaveSpiders.setTabCompleter(this);
        }
    }
}
