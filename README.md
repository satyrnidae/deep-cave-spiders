# Deep Cave Spiders
[![Java CI with Maven](https://github.com/satyrnidae/deep-cave-spiders/actions/workflows/maven.yml/badge.svg)](https://github.com/satyrnidae/deep-cave-spiders/actions)
[![CurseForge Downloads](https://cf.way2muchnoise.eu/564627.svg)](https://dev.bukkit.org/projects/deep-cave-spiders)

Today in "plugins nobody asked for", I am proud to introduce you to Deep Cave Spiders, a plugin for PaperMC 1.18 and above!

![Tiny Jockey](https://imgur.com/Asq1alJ.png)

## Why? Just... Why?

Ask yourself: When was the last time you really felt like you had to prepare before delving deep under the surface? When did you last feel that doing so was actually dangerous?

This plugin lets you have a small taste of that as you dig deeper and yet deeper into your world. An in-depth configuration system will allow you to tweak your experience as you see fit, setting minimum and maximum spawn depths and configuring spawn chance by difficulty.

Caving has rarely been so toxic!

## Metrics

This plugin uses bStats to collect some analytics about your current system setup and plugin configuration. Metrics collection is optional. If you installed a previous version of the plugin, you are already opted-out of metrics collection. If you want to opt-out on version 1.3.0 or higher, set the "metrics" option in the configuration file to "false", or comment or remove the line. Thanks!

## In-Depth Configuration

Tweak to your heart's content with in-depth configuration options, from spawn ranges to spawn chances, jockey chances, biomes, and environments!

<details><summary>Expand to Preview Default Configuration</summary>

*As of 1.3.0:*
```yaml
# The locale to use while translating chat messages.
# Default value: en_US.
locale: "en_US"
# Options for spawning the cave spiders.
spawnOptions:
  # Spawning height range.
  range:
    # The minimum Y level that cave spiders will spawn.
    # Default value: -64
    minY: -64
    # The maximum Y level that cave spiders will spawn.
    # Default value: -8
    maxY: -8
    # Allow spiders to spawn below the minimum Y height.
    # This is useful for customizing the spawn distribution ramp function; spawns below the minimum Y value are
    #   guaranteed to use a constant spawn chance as set in the chances section below.
    # Defaults to false.
    allowSpawnsBelowMinY: false
  # Spawn distribution function. Affects how often cave spiders spawn in the world.
  # Valid values are constant, hyperbolic, linear, and logarithmic
  # In order of slowest to quickest ramp-up speed:
  # - Hyperbolic distribution ramps up slowly at first but increases in likelihood the further down you go.
  # - Linear distribution ramps up at a constant rate the further down you go.
  # - Logarithmic distribution ramps up very quickly at first but slows down the further down you go.
  # - Constant distribution is the same no matter what depth the spider is spawning at.
  # Defaults to constant.
  distribution: constant
  # Chances that a cave spider will replace a normal spider within the given range.
  # Settings are between 0 and 1, separated by difficulty level.
  chances:
    # Easy mode spawn rates.
    # Default value: 0.05, or 1 in 20.
    easy: 0.05
    # Normal mode spawn rates.
    # Default value: 0.1, or 1 in 10.
    normal: 0.1
    # Hard mode spawn rated.
    # Default value: 0.5, or 1 in 2.
    hard: 0.5
  # Chance for a cave spider to spawn a baby zombie as a rider.
  # Only applies on Hard difficulty.
  # Values are between 0 and 1.
  # Defaults to 0.1, or 1 in 10 cave spiders.
  jockeyChance: 0.1
# Biomes in which the cave spiders will spawn.
# Valid values: https://papermc.io/javadocs/paper/1.18/org/bukkit/block/Biome.html
biomes:
  - badlands
  - bamboo_jungle
  - birch_forest
  - dark_forest
  - desert
  - flower_forest
  - forest
  - grove
  - jagged_peaks
  - jungle
  - meadow
  - plains
  - savanna
  - savanna_plateau
  - sparse_jungle
  - stony_peaks
  - sunflower_plains
  - swamp
  - windswept_forest
  - windswept_gravelly_hills
  - windswept_hills
  - windswept_savanna
  - wooded_badlands
# The environments in which cave spiders will spawn.
# By default, only affects NORMAL environments.
# Valid values: https://papermc.io/javadocs/paper/1.18/org/bukkit/World.Environment.html
environments:
  - normal
# A list of all of the entity types that cave spiders can replace.
# Defaults to include only spiders.
# Only replaces natural spawns.
# Valid values: https://papermc.io/javadocs/paper/1.18/org/bukkit/entity/EntityType.html
replaceEntities:
  - spider
# Whether to show debug output in the console.
# Defaults to false.
debug: false
# Whether to send usage telemetry.
# Uses bStats (https://bstats.org)
# Defaults to false, initially set to true.
metrics: true
```
</details>

### Will the plugin break if I reload?

Unfortunately, since use of the `/reload` command is inadvised, the plugin's functionality regarding the use of that command has not been verified. It could lead to multiple spiders spawning at once and config sync issues if used.

In lieu of this, the command `/deepcavespiders reload` can be used to reload and apply any changes you have made to your configuration files.

## Commands

The following commands are provided by this plugin:

<details><summary>/deepcavespiders</summary>

Used to print information about the plugin. the `reload` subcommand can be specified to reload the configuration file.
</details>

## Permissions

This plugin implements the following permissions:

<details><summary>deepcavespiders.admin</summary>

Grants access to the `/deepcavespiders reload` subcommand.
</details>

## Issues

If you encounter any issues with the plugin, please report them on our [Github Issue Tracker](https://github.com/satyrnidae/deep-cave-spiders)!
