package dev.satyrn.deepcavespiders.configuration;

import dev.satyrn.papermc.api.configuration.v4.ConfigurationConsumer;
import dev.satyrn.papermc.api.configuration.v4.ConfigurationConsumerRegistry;

public class ConfigurationRegistry extends ConfigurationConsumerRegistry<Configuration> {
    private static ConfigurationRegistry instance;

    private ConfigurationRegistry() { }

    private static ConfigurationRegistry getInstance() {
        if (instance == null) {
            instance = new ConfigurationRegistry();
        }
        return instance;
    }

    public static void registerConsumer(ConfigurationConsumer<Configuration> instance) {
        getInstance().register(instance);
    }

    public static void reloadConfiguration(Configuration configuration) {
        getInstance().reload(configuration);
    }
}
