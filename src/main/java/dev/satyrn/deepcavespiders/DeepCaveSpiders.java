package dev.satyrn.deepcavespiders;

import dev.satyrn.deepcavespiders.configuration.Configuration;
import dev.satyrn.deepcavespiders.configuration.ConfigurationRegistry;
import dev.satyrn.deepcavespiders.lang.I18n;
import dev.satyrn.deepcavespiders.listeners.SpawnEntityListener;
import dev.satyrn.deepcavespiders.util.SpawnDistribution;
import dev.satyrn.papermc.api.configuration.v4.ConfigurationConsumer;
import org.bukkit.Difficulty;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents the Deep Cave Spiders plugin.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
@SuppressWarnings("unused")
public final class DeepCaveSpiders extends JavaPlugin implements ConfigurationConsumer<Configuration> {
    // Event listener for entity spawn events.
    private SpawnEntityListener spawnEntityListener;
    // Internationalization instance.
    private I18n i18n;
    private String locale;
    private double minY;
    private double maxY;
    private double easySpawnChance;
    private double normalSpawnChance;
    private double hardSpawnChance;
    private boolean allowSpawnsBelowMinY;
    private SpawnDistribution distribution;

    /**
     * Initializes a new instance of the plugin.
     * @since 1.2-SNAPSHOT
     */
    public DeepCaveSpiders() {
        super();
        ConfigurationRegistry.registerConsumer(this);
    }

    /**
     * Called when the plugin is enabled.
     *
     * @since 1.0-SNAPSHOT
     */
    @Override
    public void onEnable() {
        this.registerEvents();

        Configuration configuration = new Configuration(this);
        this.reloadConfiguration(configuration);

        this.i18n.setLocale(this.locale);
        this.spawnEntityListener.reloadConfiguration(configuration);

        this.registerCommands();

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
        this.i18n = this.initializeI18n();
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
                    ConfigurationRegistry.reloadConfiguration(configuration);
                    sender.sendMessage(I18n.tr("command.reload"));
                } else {
                    sender.sendMessage(I18n.tr("command.reload.deny"));
                }
            } else {
                sender.sendMessage(I18n.tr("command.about",
                        this.getDescription().getName(),
                        this.getDescription().getVersion(),
                        String.join(", ", this.getDescription().getAuthors()),
                        this.maxY,
                        (this.allowSpawnsBelowMinY ? "<" : "") + this.minY,
                        this.easySpawnChance,
                        this.normalSpawnChance,
                        this.hardSpawnChance,
                        this.distribution.toString().toLowerCase(Locale.ROOT)));
                if (sender instanceof final Player player) {
                    double y = player.getLocation().getY();
                    double spawnChance = this.spawnEntityListener.getSpawnChance(player.getWorld().getDifficulty(), y) * 100;
                    if (y > this.maxY) spawnChance = 0;
                    if (y < this.minY && !this.allowSpawnsBelowMinY) spawnChance = 0;
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
     */
    private void registerEvents() {
        if (this.spawnEntityListener == null) {
            this.spawnEntityListener = new SpawnEntityListener();
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

    /**
     * Initializes the internationalization handler.
     *
     * @return The internationalization handler
     * @since 1.0-SNAPSHOT
     */
    private I18n initializeI18n() {
        // Initialize internationalization handler.
        final I18n i18n = new I18n(this);
        i18n.setLocale("en_US");
        i18n.enable();

        return i18n;
    }

    @Override
    public void reloadConfiguration(@NotNull Configuration configuration) {
        this.locale = configuration.locale.value();
        this.minY = configuration.spawnOptions.range.minY.value();
        this.maxY = configuration.spawnOptions.range.maxY.value();
        this.easySpawnChance = configuration.spawnOptions.chances.easy.value();
        this.normalSpawnChance = configuration.spawnOptions.chances.normal.value();
        this.hardSpawnChance = configuration.spawnOptions.chances.hard.value();
        this.distribution = configuration.spawnOptions.distribution.value();
        this.allowSpawnsBelowMinY = configuration.spawnOptions.range.allowSpawnsBelowMinY.value();
    }
}
