package hu.webarticum.resourcemanager.config;

import java.util.HashMap;
import java.util.Map;

import hu.webarticum.resourcemanager.resource.ResourceKey;

/**
 * Extracts subproperties found under a key prefix
 */
public class SubPropertiesConfigItemDefinition
        implements PropertiesConfigItemDefinition<Map<String, String>> {

    private final ResourceKey<Map<String, String>> key;

    private final String prefix;


    public SubPropertiesConfigItemDefinition(
            ResourceKey<Map<String, String>> key, String prefix) {
        
        this.key = key;
        this.prefix = prefix;
    }


    @Override
    public ResourceKey<Map<String, String>> getKey() {
        return key;
    }

    @Override
    public Map<String, String> extractValue(Map<String, String> properties) throws Exception {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            if (propertyName.startsWith(prefix)) {
                String subName = propertyName.substring(prefix.length());
                result.put(subName, entry.getValue());
            }
        }
        return result;
    }

}
