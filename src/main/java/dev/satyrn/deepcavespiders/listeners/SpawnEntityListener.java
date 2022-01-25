package dev.satyrn.deepcavespiders.listeners;

import dev.satyrn.deepcavespiders.configuration.Configuration;
import dev.satyrn.papermc.api.util.v1.MathHelper;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Spawns cave spiders according to configuration.
 *
 * @author Isabel Maskrey
 * @since 1.0-SNAPSHOT
 */
@SuppressWarnings("ClassCanBeRecord")
public class SpawnEntityListener implements Listener {
    private final @NotNull Plugin plugin;
    private final @NotNull Configuration configuration;

    public SpawnEntityListener(final @NotNull Plugin plugin, final @NotNull Configuration configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
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
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL || !this.configuration.replaceEntities.value()
                .contains(event.getEntity().getType())) {
            return;
        }
        // Location validation. Should only spawn between min and max spawn heights,
        // and should not spawn in liquids.
        final Location location = event.getLocation();
        if ((location.getBlockY() < this.configuration.spawnOptions.range.minY.value() && !this.configuration.spawnOptions.range.allowSpawnsBelowMinY.value()) || location.getBlockY() > this.configuration.spawnOptions.range.maxY.value() || location.getBlock()
                .isLiquid()) {
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
        final Biome biome = world.getBiome(location);
        if (!this.configuration.environments.value()
                .contains(world.getEnvironment()) || !this.configuration.biomes.value().contains(biome)) {
            return;
        }
        // Check if the spawn occurs.
        if (Math.random() <= spawnChance) {
            event.setCancelled(true);
            this.plugin.getLogger()
                    .log(Level.FINEST, "[Events] Replaced {0} with cave spider in a {1} at x:{2}, y:{3}, z:{4} in world {5} with environment {6} at a chance of {7}%.", new Object[]{event.getEntity().getType(), biome, location.getX(), location.getY(), location.getZ(), world.getName(), world.getEnvironment(), spawnChance * 100});
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
                final double jockeyChance = this.configuration.spawnOptions.jockeyChance.value();
                // Spawn a baby zombie as a jockey, if spawn chance is > 0 and world difficulty is set to hard.
                if (this.configuration.spawnOptions.jockeyChance.value() > 0D && world.getDifficulty() == Difficulty.HARD && Math.random() <= jockeyChance) {
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
                    this.plugin.getLogger()
                            .log(Level.FINER, "[Events] Spawned {0} jockey for cave spider in a {1} at x:{2}, y:{3}, z:{4} in world {5} with environment {6} at a chance of {7}%.", new Object[]{jockeyType, biome, location.getX(), location.getY(), location.getZ(), world.getName(), environment, jockeyChance * 100});
                }
            }
        }
    }

    /**
     * Gets the spawn chance for a specific difficulty.
     *
     * @param difficulty The world difficulty.
     * @param y          The Y location of the spawn. Used if spawn distribution is not set to CONSTANT.
     * @return The spawn chance for the given difficulty.
     */
    public double getSpawnChance(Difficulty difficulty, double y) {
        final double defaultSpawnChance = this.configuration.spawnOptions.chances.value(difficulty);
        double spawnChance = defaultSpawnChance;

        int maxY = this.configuration.spawnOptions.range.maxY.value();
        int minY = this.configuration.spawnOptions.range.minY.value();

        switch (this.configuration.spawnOptions.distribution.value()) {
            case LINEAR -> spawnChance *= ((-y + maxY) / (maxY - minY));
            case HYPERBOLIC -> spawnChance *= (Math.pow((-y + maxY) / (maxY - minY), 2));
            case LOGARITHMIC -> spawnChance *= MathHelper.logb(-y + maxY, maxY - minY);
        }
        return MathHelper.clampd(spawnChance, 0D, Math.max(0D, defaultSpawnChance));
    }
}
