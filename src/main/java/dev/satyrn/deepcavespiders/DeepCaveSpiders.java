package dev.satyrn.deepcavespiders;

import dev.satyrn.deepcavespiders.configuration.Configuration;
import dev.satyrn.deepcavespiders.lang.I18n;
import dev.satyrn.deepcavespiders.listeners.SpawnEntityListener;
import org.bukkit.Difficulty;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DeepCaveSpiders extends JavaPlugin {
    private SpawnEntityListener spawnEntityListener;
    private Configuration configuration;
    private I18n i18n;

    @Override
    public void onEnable() {
        this.registerEvents();
        this.configuration = new Configuration(this);
        this.i18n.setLocale(configuration.getLocale());
        spawnEntityListener.load(configuration);
        this.registerCommands();
        this.getLogger().info("DeepCaveSpiders has been successfully enabled!");
    }

    @Override
    public void onLoad() {
        this.getLogger().info("Initializing DeepCaveSpiders...");
        this.saveDefaultConfig();
        this.i18n = this.initializeI18n();
    }

    /**
     * {@inheritDoc}
     *
     * @param sender
     * @param command
     * @param alias
     * @param args
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completionOptions = new ArrayList<>();
        if ("deepcavespiders".equalsIgnoreCase(command.getName())) {
            if (args.length == 1) {
                if (command.testPermission(sender)) {
                    completionOptions.add("reload");
                }
            }
        }
        return completionOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if ("deepcavespiders".equalsIgnoreCase(command.getName())) {
            if (args.length >= 1 && "reload".equalsIgnoreCase(args[0])) {
                if (command.testPermission(sender)) {
                    this.reloadConfig();
                    final Configuration configuration = new Configuration(this);
                    this.spawnEntityListener.load(configuration);
                    sender.sendMessage(I18n.tr("command.reload"));
                } else {
                    sender.sendMessage(I18n.tr("command.reload.deny"));
                }
            } else {
                sender.sendMessage(I18n.tr("command.about",
                        this.getDescription().getVersion(),
                        this.spawnEntityListener.maxSpawnHeight,
                        this.spawnEntityListener.minSpawnHeight,
                        this.spawnEntityListener.getSpawnChance(Difficulty.EASY),
                        this.spawnEntityListener.getSpawnChance(Difficulty.NORMAL),
                        this.spawnEntityListener.getSpawnChance(Difficulty.HARD)));
            }
            return true;
        }
        return false;
    }


    private void registerEvents() {
        if (this.spawnEntityListener == null) {
            this.spawnEntityListener = new SpawnEntityListener();
            final PluginManager pluginManager = this.getServer().getPluginManager();
            pluginManager.registerEvents(this.spawnEntityListener, this);
        }
        this.spawnEntityListener = new SpawnEntityListener();
        this.getServer().getPluginManager().registerEvents(this.spawnEntityListener, this);
    }

    private void registerCommands() {
        final PluginCommand deepCaveSpiders = this.getServer().getPluginCommand("deepcavespiders");
        if (deepCaveSpiders != null) {
            deepCaveSpiders.setExecutor(this);
            deepCaveSpiders.setTabCompleter(this);
        }
    }


    /**
     * Initializes the internationalization handler.
     */
    private I18n initializeI18n() {
        // Initialize internationalization handler.
        final I18n i18n = new I18n(this);
        i18n.setLocale("en_US");
        i18n.enable();

        return i18n;
    }
}
