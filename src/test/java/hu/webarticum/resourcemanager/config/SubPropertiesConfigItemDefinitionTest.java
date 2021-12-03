package hu.webarticum.resourcemanager.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hu.webarticum.resourcemanager.common.CastUtil;
import hu.webarticum.resourcemanager.resource.ResourceKey;

class SubPropertiesConfigItemDefinitionTest {

    private static final ResourceKey<Map<String, String>> KEY =
            new ResourceKey<>("test-map", CastUtil.castClass(Map.class));

    private static final String PREFIX = "name.prefix.";


    @Test
    void testEmpty() throws Exception {
        Map<String, String> properties = new HashMap<>();
        SubPropertiesConfigItemDefinition definition = create();
        Map<String, String> actual = definition.extractValue(properties);
        assertThat(actual).isEmpty();
    }

    @Test
    void testNoOneFound() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("", "");
        properties.put("lorem", "ipsum");
        properties.put("apple", "banana");
        properties.put("orange", "name.prefix.key.sub");
        properties.put("NAME.PREFIX.HELLO", "WORLD");

        SubPropertiesConfigItemDefinition definition = create();
        Map<String, String> actual = definition.extractValue(properties);
        assertThat(actual).isEmpty();
    }

    @Test
    void testSomeFound() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("", "");
        properties.put("lorem", "ipsum");
        properties.put("name.prefix", "apple");
        properties.put("name.prefix.", "banana");
        properties.put("XXX", "YYY");
        properties.put("name.prefix.key.sub", "orange");
        properties.put("orange", "name.prefix.key.sub");
        properties.put("NAME.PREFIX.HELLO", "WORLD");

        SubPropertiesConfigItemDefinition definition = create();
        Map<String, String> actual = definition.extractValue(properties);

        Map<String, String> expected = new HashMap<>();
        expected.put("", "banana");
        expected.put("key.sub", "orange");

        assertThat(actual).isEqualTo(expected);
    }

    private static SubPropertiesConfigItemDefinition create() {
        return new SubPropertiesConfigItemDefinition(KEY, PREFIX);
    }

}
