package hu.webarticum.resourcemanager.config;

import java.io.IOException;

/**
 * Interface for configuration loaders
 */
public interface ConfigLoader {

    /**
     * Reloads the underlying configuration
     *
     * @throws IllegalArgumentException If an invalid value found
     * @throws IOException If any other error occured
     */
    public void reload() throws IOException;

}
