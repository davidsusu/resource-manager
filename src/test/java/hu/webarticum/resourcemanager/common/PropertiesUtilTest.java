package hu.webarticum.resourcemanager.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class PropertiesUtilTest {

    @Test
    void testLoadFromResource() {
        Properties properties = PropertiesUtil.loadBundled(
                getClass().getClassLoader(),
                "hu/webarticum/resourcemanager/common/test.properties");
        assertThat(properties.containsKey("xxx")).isFalse();
        assertThat(properties.get("lorem")).isEqualTo("ipsum");
        assertThat(properties.get("foo")).isEqualTo("bar");
    }

}
