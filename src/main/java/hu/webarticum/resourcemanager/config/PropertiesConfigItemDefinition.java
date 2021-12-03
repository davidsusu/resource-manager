package hu.webarticum.resourcemanager.config;

import java.util.Map;

import hu.webarticum.resourcemanager.resource.ResourceKey;

/**
 * Interface for high-level configuration items
 *
 * @param <T> Resource type
 */
public interface PropertiesConfigItemDefinition<T> {

    /**
     * Gets the associated {@link ResourceKey}
     *
     * @return The resource key
     */
    ResourceKey<T> getKey();

    /**
     * Extracts a high-level value from low-level properties
     *
     * @param properties
     * @return The high-level config value
     * @throws Exception If any error occured
     */
    T extractValue(Map<String, String> properties) throws Exception; // NOSONAR

}
