package dev.satyrn.deepcavespiders.listeners;

import dev.satyrn.deepcavespiders.configuration.Configuration;
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
public class SpawnEntityListener implements Listener {
    // Maximum spawn height.
    public int maxSpawnHeight;
    // Minimum spawn height.
    public int minSpawnHeight;
    // Spawn chances for easy difficulty.
    private double easySpawnChance;
    // Spawn chances for normal difficulty.
    private double normalSpawnChance;
    // Spawn chances for hard difficulty.
    private double hardSpawnChance;
    private double riderChance;
    @NotNull private List<Biome> biomes = new ArrayList<>();
    @NotNull private List<World.Environment> environments = new ArrayList<>();

    /**
     * Loads the configuration instance.
     *
     * @param configuration The configuration instance.
     * @since 1.0-SNAPSHOT
     */
    public void load(Configuration configuration) {
        this.maxSpawnHeight = configuration.getMaxY();
        this.minSpawnHeight = configuration.getMinY();
        this.riderChance = configuration.getJockeyChance();
        this.biomes = configuration.getBiomes();
        this.environments = configuration.getEnvironments();
        this.easySpawnChance = configuration.getSpawnChance(Difficulty.EASY);
        this.normalSpawnChance = configuration.getSpawnChance(Difficulty.NORMAL);
        this.hardSpawnChance = configuration.getSpawnChance(Difficulty.HARD);
    }

    /**
     * Handles the entity spawn event. If all conditions are met the
     * spawned entity is replaced with a cave spider.
     * @param event The event.
     */
    @EventHandler
    public void onSpawnSpider(CreatureSpawnEvent event) {
        // Only replace spider spawns, but exclude cave spider spawns,
        // and don't replace jockeys.
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
                || !(event.getEntity() instanceof Spider)
                || event.getEntity() instanceof CaveSpider) {
            return;
        }
        // Location validation. Should only spawn between min and max spawn heights,
        // and should not spawn in liquids.
        final Location location = event.getLocation();
        if (location.getBlockY() < this.minSpawnHeight
                || location.getBlockY() > this.maxSpawnHeight
                || location.getBlock().isLiquid()) {
            return;
        }
        // World validation. Should only spawn in configured environments and biomes,
        // and on the configured difficulties.
        final World world = location.getWorld();
        final double spawnChance = this.getSpawnChance(world.getDifficulty());
        // We'll never spawn if the spawn chance is zero.
        if (spawnChance <= 0D) {
            return;
        }
        if (!this.environments.contains(world.getEnvironment())
                || !this.biomes.contains(world.getBiome(location))) {
            return;
        }

        // Check if the spawn occurs.
        if (Math.random() < spawnChance) {
            event.setCancelled(true);
            world.spawnEntity(location, EntityType.CAVE_SPIDER, CreatureSpawnEvent.SpawnReason.NATURAL);
        }
    }

    @EventHandler
    public void onSpawnCaveSpider(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG) {
            final Entity entity = event.getEntity();
            if (entity instanceof CaveSpider) {
                final Location location = event.getLocation();
                final World world = location.getWorld();

                // Spawn a baby zombie as a jockey, if spawn chance is > 0 and world difficulty is set to hard.
                if (this.riderChance > 0D
                        && world.getDifficulty() == Difficulty.HARD
                        && Math.random() < this.riderChance) {
                    final Biome biome = world.getBiome(location);
                    final @NotNull EntityType jockeyType;
                    switch (biome) {
                        case DESERT -> jockeyType = EntityType.HUSK;
                        case NETHER_WASTES, CRIMSON_FOREST, WARPED_FOREST, SOUL_SAND_VALLEY -> jockeyType = EntityType.ZOMBIFIED_PIGLIN;
                        default -> jockeyType = EntityType.ZOMBIE;

                    }
                    final @NotNull Zombie jockey = (Zombie) world.spawnEntity(location, jockeyType, CreatureSpawnEvent.SpawnReason.JOCKEY);
                    jockey.setBaby();
                    entity.addPassenger(jockey);
                }
            }
        }
    }

    public double getSpawnChance(Difficulty difficulty) {
        switch (difficulty) {
            case EASY -> {
                return this.easySpawnChance;
            }
            case NORMAL -> {
                return this.normalSpawnChance;
            }
            case HARD -> {
                return this.hardSpawnChance;
            }
        }
        return 0D;
    }
}
