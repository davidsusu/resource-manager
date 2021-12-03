package hu.webarticum.resourcemanager.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hu.webarticum.resourcemanager.common.CastUtil;
import hu.webarticum.resourcemanager.resource.ResourceKey;
import hu.webarticum.resourcemanager.resource.ResourceManager;

/**
 * Basic {@link ConfigLoader} implementation.
 *
 * Loads low-level properties from one or more {@link PropertiesSupplier},
 * and synchronize the derived high-level items with a {@link ResourceManager}.
 * If no error occured, the changed items will be closed in the
 * {@link ResourceManager}, so the dependant resources will restart.
 */
public class PropertiesConfigLoader implements ConfigLoader {

    private final ResourceManager resourceManager;

    private final List<PropertiesConfigItemDefinition<?>> definitions;

    private final List<PropertiesSupplier> suppliers;

    private final Validator validator;


    private Map<ResourceKey<?>, Object> loadedConfigItems = null;


    public PropertiesConfigLoader(
            ResourceManager resourceManager,
            Collection<PropertiesConfigItemDefinition<?>> definitions,
            Collection<PropertiesSupplier> suppliers) {

        this(resourceManager, definitions, suppliers, properties -> {});
    }
    
    public PropertiesConfigLoader(
            ResourceManager resourceManager,
            Collection<PropertiesConfigItemDefinition<?>> definitions,
            Collection<PropertiesSupplier> suppliers,
            Validator validator) {

        this.resourceManager = resourceManager;
        this.definitions = new ArrayList<>(definitions);
        this.suppliers = new ArrayList<>(suppliers);
        this.validator = validator;
        registerDefinitionsToResourceManager();
    }

    private void registerDefinitionsToResourceManager() {
        for (PropertiesConfigItemDefinition<?> definition : definitions) {
            ResourceKey<Object> key = CastUtil.cast(definition.getKey());
            resourceManager.register(key, (resourceManagerParam, keyParam) -> {
                if (loadedConfigItems == null) {
                    throw new IllegalStateException(
                            String.format("Config for '%s' is not loaded yet", definition.getKey()));
                }
                return loadedConfigItems.get(keyParam);
            }, (resourceManagerParam, keyParam, value) -> {});

        }
    }

    // repeated because Eclipse does not inherit the IllegalArgumentException
    /**
     * Reloads the underlying configuration
     *
     * @throws IllegalArgumentException If an invalid value found
     * @throws IOException If any other error occured
     */
    @Override
    public synchronized void reload() throws IOException {
        Map<ResourceKey<?>, Object> newConfigItems = load();
        try {
            validator.validate(newConfigItems);
        } catch (IllegalArgumentException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Configuration validation failed", e);
        }
        if (loadedConfigItems != null) {
            closeChangedConfigItems(newConfigItems);
        }
        loadedConfigItems = newConfigItems;
    }

    private void closeChangedConfigItems(Map<ResourceKey<?>, Object> newConfigItems) {
        for (PropertiesConfigItemDefinition<?> definition : definitions) {
            ResourceKey<?> key = definition.getKey();
            if (!Objects.equals(newConfigItems.get(key), loadedConfigItems.get(key))) {
                resourceManager.close(key);
            }
        }
    }

    private Map<ResourceKey<?>, Object> load() throws IOException { // NOSONAR
        try {
            return loadWithAnyException();
        } catch (IOException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private Map<ResourceKey<?>, Object> loadWithAnyException() throws Exception { // NOSONAR
        Map<ResourceKey<?>, Object> result = new HashMap<>();
        Map<String, String> properties = loadProperties();
        for (PropertiesConfigItemDefinition<?> definition : definitions) {
            result.put(definition.getKey(), definition.extractValue(properties));
        }
        return result;
    }

    private Map<String, String> loadProperties() throws Exception {
        Map<String, String> result = new HashMap<>();
        for (PropertiesSupplier supplier : suppliers) {
            for (Map.Entry<?, ?> entry : supplier.get().entrySet()) {
                String valueString = Objects.toString(entry.getValue(), null);
                if (valueString != null) {
                    String keyString = entry.getKey().toString();
                    result.put(keyString, valueString);
                }
            }
        }
        return result;
    }
    
    
    public interface Validator {
        
        public void validate(Map<ResourceKey<?>, Object> properties) throws Exception; // NOSONAR
        
    }

}
