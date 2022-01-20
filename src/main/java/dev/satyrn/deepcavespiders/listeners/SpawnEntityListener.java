package dev.satyrn.deepcavespiders.listeners;

import dev.satyrn.deepcavespiders.configuration.Configuration;
import dev.satyrn.deepcavespiders.configuration.ConfigurationRegistry;
import dev.satyrn.deepcavespiders.util.SpawnDistribution;
import dev.satyrn.papermc.api.configuration.v4.ConfigurationConsumer;
import dev.satyrn.papermc.api.util.v1.MathHelper;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawns cave spiders according to configuration.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
public class SpawnEntityListener implements Listener, ConfigurationConsumer<Configuration> {
    // Maximum spawn height.
    public int maxY;
    // Minimum spawn height.
    public int minY;
    // Spawn chances for easy difficulty.
    private double easySpawnChance;
    // Spawn chances for normal difficulty.
    private double normalSpawnChance;
    // Spawn chances for hard difficulty.
    private double hardSpawnChance;
    // Chance that a jockey will spawn on a cave spider.
    private double jockeyChance;
    // The list of allowed biomes.
    @NotNull
    private List<Biome> biomes = new ArrayList<>();
    // The list of allowed environments.
    @NotNull
    private List<World.Environment> environments = new ArrayList<>();
    @NotNull
    private SpawnDistribution spawnDistribution = SpawnDistribution.CONSTANT;
    // Whether spawns will occur below the minimum Y value.
    private boolean allowSpawnsBelowMinY;
    @NotNull
    private List<EntityType> replaceEntities = new ArrayList<>();

    public SpawnEntityListener() {
        ConfigurationRegistry.registerConsumer(this);
    }

    /**
     * Loads the configuration instance.
     *
     * @param configuration The configuration instance.
     * @since 1.0-SNAPSHOT
     */
    public void reloadConfiguration(Configuration configuration) {
        this.maxY = configuration.spawnOptions.range.maxY.value();
        this.minY = configuration.spawnOptions.range.minY.value();
        this.jockeyChance = configuration.spawnOptions.jockeyChance.value();
        this.biomes = configuration.biomes.value();
        this.environments = configuration.environments.value();
        this.easySpawnChance = configuration.spawnOptions.chances.easy.value();
        this.normalSpawnChance = configuration.spawnOptions.chances.normal.value();
        this.hardSpawnChance = configuration.spawnOptions.chances.hard.value();
        this.spawnDistribution = configuration.spawnOptions.distribution.value();
        this.allowSpawnsBelowMinY = configuration.spawnOptions.range.allowSpawnsBelowMinY.value();
        this.replaceEntities = configuration.replaceEntities.value();
    }

    /**
     * Handles creature spawn events for spiders.
     *
     * @param event The event.
     */
    @EventHandler
    public void onSpawnSpider(CreatureSpawnEvent event) {
        // Only replace spider spawns, but exclude cave spider spawns,
        // and don't replace jockeys.
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL || !this.replaceEntities.contains(event.getEntity().getType())) {
            return;
        }
        // Location validation. Should only spawn between min and max spawn heights,
        // and should not spawn in liquids.
        final Location location = event.getLocation();
        if ((location.getBlockY() < this.minY && !this.allowSpawnsBelowMinY) || location.getBlockY() > this.maxY || location.getBlock().isLiquid()) {
            return;
        }
        // World validation. Should only spawn in configured environments and biomes,
        // and on the configured difficulties.
        final World world = location.getWorld();
        final double spawnChance = this.getSpawnChance(world.getDifficulty(), location.getY());
        // We'll never spawn if the spawn chance is zero.
        if (spawnChance <= 0D) {
            return;
        }
        if (!this.environments.contains(world.getEnvironment()) || !this.biomes.contains(world.getBiome(location))) {
            return;
        }

        // Check if the spawn occurs.
        if (Math.random() < spawnChance) {
            event.setCancelled(true);
            world.spawnEntity(location, EntityType.CAVE_SPIDER, CreatureSpawnEvent.SpawnReason.NATURAL);
        }
    }

    /**
     * Handles creature spawn events for cave spiders.
     *
     * @param event The event.
     */
    @EventHandler
    public void onSpawnCaveSpider(CreatureSpawnEvent event) {
        // Only apply to naturally / egg spawned creatures.
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            // Make sure the creature is a cave spider.
            final Entity entity = event.getEntity();
            if (entity instanceof CaveSpider) {

                final Location location = event.getLocation();
                final World world = location.getWorld();

                // Spawn a baby zombie as a jockey, if spawn chance is > 0 and world difficulty is set to hard.
                if (this.jockeyChance > 0D && world.getDifficulty() == Difficulty.HARD && Math.random() < this.jockeyChance) {
                    final Biome biome = world.getBiome(location);
                    final World.Environment environment = world.getEnvironment();
                    final @NotNull EntityType jockeyType;
                    if (environment == World.Environment.NETHER) {
                        jockeyType = EntityType.ZOMBIFIED_PIGLIN;
                    } else {
                        switch (biome) {
                            case DESERT -> jockeyType = EntityType.HUSK;
                            case DEEP_COLD_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, DEEP_OCEAN, COLD_OCEAN, FROZEN_OCEAN, LUKEWARM_OCEAN, OCEAN, WARM_OCEAN, RIVER, FROZEN_RIVER -> jockeyType = EntityType.DROWNED;
                            default -> jockeyType = EntityType.ZOMBIE;
                        }
                    }
                    final @NotNull Zombie jockey = (Zombie) world.spawnEntity(location, jockeyType, CreatureSpawnEvent.SpawnReason.JOCKEY);
                    jockey.setBaby();
                    entity.addPassenger(jockey);
                }
            }
        }
    }

    /**
     * Gets the spawn chance for a specific difficulty.
     *
     * @param difficulty The world difficulty.
     * @param y The Y location of the spawn. Used if spawn distribution is not set to CONSTANT.
     * @return The spawn chance for the given difficulty.
     */
    public double getSpawnChance(Difficulty difficulty, double y) {
        final double defaultSpawnChance = difficulty == Difficulty.HARD ? this.hardSpawnChance : difficulty == Difficulty.NORMAL ? this.normalSpawnChance : difficulty == Difficulty.EASY ? this.easySpawnChance : 0D;
        double spawnChance = defaultSpawnChance;

        switch (this.spawnDistribution) {
            case LINEAR -> spawnChance *= ((-y + this.maxY)/(this.maxY - this.minY));
            case HYPERBOLIC -> spawnChance *= (Math.pow((-y + this.maxY)/(this.maxY - this.minY), 2));
            case LOGARITHMIC -> spawnChance *= MathHelper.logb(-y + this.maxY, this.maxY - this.minY);
        }
        return MathHelper.clampd(spawnChance, 0D, Math.max(0D, defaultSpawnChance));
    }
}
