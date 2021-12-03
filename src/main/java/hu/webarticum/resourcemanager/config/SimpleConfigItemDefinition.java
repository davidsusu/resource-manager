package hu.webarticum.resourcemanager.config;

import java.util.Map;

import hu.webarticum.resourcemanager.resource.ResourceKey;

/**
 * Simple {@link PropertiesConfigItemDefinition} implementation with textual value parsing
 */
public class SimpleConfigItemDefinition<T> implements PropertiesConfigItemDefinition<T> {

    private final ResourceKey<T> key;

    private final String propertyName;

    private final ValueParser<T> parser;


    public SimpleConfigItemDefinition(
            ResourceKey<T> key, String propertyName, ValueParser<T> parser) {
        
        this.key = key;
        this.propertyName = propertyName;
        this.parser = parser;
    }


    @Override
    public ResourceKey<T> getKey() {
        return key;
    }

    @Override
    public T extractValue(Map<String, String> properties) throws Exception {
        String value = properties.get(propertyName);
        if (value == null) {
            throw new IllegalArgumentException(
                    String.format("Property value not found at '%s'", propertyName));
        }

        return parser.parse(value);
    }

}
