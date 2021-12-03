package hu.webarticum.resourcemanager.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hu.webarticum.resourcemanager.common.PropertiesUtil;
import hu.webarticum.resourcemanager.resource.DefaultResourceManager;
import hu.webarticum.resourcemanager.resource.OpeningFailedException;
import hu.webarticum.resourcemanager.resource.ResourceKey;
import hu.webarticum.resourcemanager.resource.ResourceManager;

class PropertiesConfigLoaderTest {

    private static final ResourceKey<BigInteger> BIG_INTEGER_KEY =
            new ResourceKey<>("config-big-integer", BigInteger.class);

    private static final ResourceKey<Boolean> BOOLEAN_KEY =
            new ResourceKey<>("config-boolean", Boolean.class);


    @Test
    void testIllegalState() throws IOException {
        List<PropertiesConfigItemDefinition<?>> definitions = Arrays.asList(
                new SimpleConfigItemDefinition<>(
                        BIG_INTEGER_KEY, "number", ValueParsers.BIG_INTEGER));
        List<PropertiesSupplier> suppliers = Arrays.asList(
                () -> PropertiesUtil.buildFrom("number", "35"));
        ResourceManager resourceManager = new DefaultResourceManager();
        PropertiesConfigLoader configLoader = new PropertiesConfigLoader(
                resourceManager, definitions, suppliers);

        assertThatThrownBy(() -> resourceManager.open(BIG_INTEGER_KEY))
                .isInstanceOf(OpeningFailedException.class);

        configLoader.reload();

        assertThatCode(() -> resourceManager.open(BIG_INTEGER_KEY)).doesNotThrowAnyException();
    }

    @Test
    void testIllegalArgument() throws IOException {
        List<PropertiesConfigItemDefinition<?>> definitions = Arrays.asList(
                new SimpleConfigItemDefinition<>(
                        BIG_INTEGER_KEY, "number", ValueParsers.BIG_INTEGER));
        List<PropertiesSupplier> suppliers = Arrays.asList(
                () -> PropertiesUtil.buildFrom("number", "potato"));
        ResourceManager resourceManager = new DefaultResourceManager();
        PropertiesConfigLoader configLoader = new PropertiesConfigLoader(
                resourceManager, definitions, suppliers);

        assertThatThrownBy(() -> configLoader.reload())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBasic() throws IOException {
        List<PropertiesConfigItemDefinition<?>> definitions = Arrays.asList(
                new SimpleConfigItemDefinition<>(
                        BIG_INTEGER_KEY, "number", ValueParsers.BIG_INTEGER),
                new SimpleConfigItemDefinition<>(
                        BOOLEAN_KEY, "switch", ValueParsers.BOOLEAN));
        List<PropertiesSupplier> suppliers = Arrays.asList(
                () -> PropertiesUtil.buildFrom(
                        "number", "32",
                        "switch", "on"));
        ResourceManager resourceManager = new DefaultResourceManager();
        PropertiesConfigLoader configLoader = new PropertiesConfigLoader(
                resourceManager, definitions, suppliers);

        configLoader.reload();

        assertThat(resourceManager.open(BIG_INTEGER_KEY)).isEqualTo(BigInteger.valueOf(32));
        assertThat(resourceManager.open(BOOLEAN_KEY)).isTrue();
    }

    @Test
    void testChange() throws IOException {
        List<PropertiesConfigItemDefinition<?>> definitions = Arrays.asList(
                new SimpleConfigItemDefinition<>(
                        BIG_INTEGER_KEY, "number", ValueParsers.BIG_INTEGER),
                new SimpleConfigItemDefinition<>(
                        BOOLEAN_KEY, "switch", ValueParsers.BOOLEAN));

        Map<String, String> data = new HashMap<>();
        data.put("number", "13");
        data.put("switch", "off");
        data.put("lorem", "ipsum");

        List<PropertiesSupplier> suppliers = Arrays.asList(() -> data);
        ResourceManager resourceManager = new DefaultResourceManager();
        PropertiesConfigLoader configLoader = new PropertiesConfigLoader(
                resourceManager, definitions, suppliers);

        configLoader.reload();

        assertThat(resourceManager.open(BIG_INTEGER_KEY)).isEqualTo(BigInteger.valueOf(13));
        assertThat(resourceManager.open(BOOLEAN_KEY)).isFalse();

        data.put("number", "21");
        data.put("switch", "on");
        configLoader.reload();

        assertThat(resourceManager.open(BIG_INTEGER_KEY)).isEqualTo(BigInteger.valueOf(21));
        assertThat(resourceManager.open(BOOLEAN_KEY)).isTrue();
    }

    @Test
    void testOverride() throws IOException {
        List<PropertiesConfigItemDefinition<?>> definitions = Arrays.asList(
                new SimpleConfigItemDefinition<>(
                        BIG_INTEGER_KEY, "number", ValueParsers.BIG_INTEGER),
                new SimpleConfigItemDefinition<>(
                        BOOLEAN_KEY, "switch", ValueParsers.BOOLEAN));

        Map<String, String> data1 = new HashMap<>();
        data1.put("number", "9");
        data1.put("switch", "on");
        data1.put("lorem", "ipsum");

        Map<String, String> data2 = new HashMap<>();
        data2.put("number", "14");

        List<PropertiesSupplier> suppliers = Arrays.asList(() -> data1, () -> data2);
        ResourceManager resourceManager = new DefaultResourceManager();
        PropertiesConfigLoader configLoader = new PropertiesConfigLoader(
                resourceManager, definitions, suppliers);

        configLoader.reload();

        assertThat(resourceManager.open(BIG_INTEGER_KEY)).isEqualTo(BigInteger.valueOf(14));
        assertThat(resourceManager.open(BOOLEAN_KEY)).isTrue();

        data2.remove("number");
        data2.put("switch", "off");
        configLoader.reload();

        assertThat(resourceManager.open(BIG_INTEGER_KEY)).isEqualTo(BigInteger.valueOf(9));
        assertThat(resourceManager.open(BOOLEAN_KEY)).isFalse();
    }

}
