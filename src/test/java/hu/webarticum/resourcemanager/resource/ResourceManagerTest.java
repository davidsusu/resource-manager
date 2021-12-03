package hu.webarticum.resourcemanager.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResourceManagerTest {

    private enum ManagerContentKind { EMPTY, DEFAULT };
    
    private static Map<Class<? extends ResourceManager>, Function<String, ResourceManager>> implementations =
            new HashMap<>();
    
    static {
        implementations.put(DefaultResourceManager.class, label -> new DefaultResourceManager(label));
    }
    
    
    @ParameterizedTest
    @MethodSource("provideEmpty")
    void testBasicsOfEmpty(ResourceManager resourceManager) {
        String label = resourceManager.getLabel();
        
        assertThat(resourceManager).as("string").hasToString(label);
        assertThat(resourceManager).matches(r -> r.isEmpty(), "should be empty");
        assertThat(resourceManager.size()).as("size").isZero();
        assertThat(resourceManager.keySet()).as("keys").isEmpty();
        assertThat(resourceManager.containsKey(key("foo"))).as("contains foo").isFalse();
        assertThat(resourceManager.hasOpen()).as("has open").isFalse();
        assertThat(resourceManager.countOpen()).as("open count").isZero();
        assertThat(resourceManager.openKeySet()).as("open keys").isEmpty();
        assertThat(resourceManager.closedKeySet()).as("closed keys").isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testBasicsOfFilled(ResourceManager resourceManager) {
        assertThat(resourceManager).matches(r -> !r.isEmpty(), "should not be empty");
        assertThat(resourceManager.size()).as("size").isEqualTo(10);
        assertThat(resourceManager.keySet()).as("keys").isEqualTo(keys(
                "light", "camera", "cameraman", "shot", "movie",
                "fire", "cookbook", "chef", "cooking", "cookingshow"));
        assertThat(resourceManager.containsKey(key("foo"))).as("contains foo").isFalse();
        assertThat(resourceManager.containsKey(key("light"))).as("contains foo").isTrue();
        assertThat(resourceManager.hasOpen()).as("has open").isFalse();
        assertThat(resourceManager.countOpen()).as("open count").isZero();
        assertThat(resourceManager.openKeySet()).as("open keys").isEmpty();
        assertThat(resourceManager.closedKeySet()).as("closed keys").isEqualTo(keys(
                "light", "camera", "cameraman", "shot", "movie",
                "fire", "cookbook", "chef", "cooking", "cookingshow"));
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testKeyDuplication(ResourceManager resourceManager) {
        assertThatThrownBy(() -> resourceManager
                .register(key("light"), ResourceManagerTest::create)).as("duplicate light")
                .isInstanceOf(DuplicateKeyException.class);
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testOpen(ResourceManager resourceManager) {
        String label = resourceManager.getLabel();
        ResourceKey<TestResource> lightKey = key("light");
        ResourceKey<TestResource> fireKey = key("fire");
        resourceManager.open(lightKey);

        assertThat(resourceManager).as("string").hasToString(label);
        assertThat(resourceManager.hasOpen()).as("has open").isTrue();
        assertThat(resourceManager.countOpen()).as("open count").isEqualTo(1);
        assertThat(resourceManager.openKeySet()).as("open keys").isEqualTo(keys("light"));
        assertThat(resourceManager.closedKeySet()).as("closed keys").isEqualTo(keys(
                "camera", "cameraman", "shot", "movie",
                "fire", "cookbook", "chef", "cooking", "cookingshow"));
        assertThat(resourceManager.isOpen(lightKey)).as("light is open").isTrue();
        assertThat(resourceManager.isOpen(fireKey)).as("light is open").isFalse();
        assertThat(resourceManager.get(lightKey).isClosed()).as("light is off").isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testOpenComplex1(ResourceManager resourceManager) {
        resourceManager.open(key("movie"));
        
        assertThat(resourceManager.openKeySet()).as("open keys").isEqualTo(keys(
                "light", "camera", "cameraman", "shot", "movie"));
        assertThat(resourceManager.closedKeySet()).as("closed keys").isEqualTo(keys(
                "fire", "cookbook", "chef", "cooking", "cookingshow"));
        assertThat(resourceManager.hasOpen()).as("has open").isTrue();
        assertThat(resourceManager.countOpen()).as("open count").isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testOpenComplex2(ResourceManager resourceManager) {
        resourceManager.open(key("cookingshow"));
        
        assertThat(resourceManager.openKeySet()).as("openKeys").isEqualTo(keys(
                "light", "camera", "cameraman", "shot",
                "fire", "cookbook", "chef", "cooking", "cookingshow"));
        assertThat(resourceManager.closedKeySet()).as("closed keys").isEqualTo(keys("movie"));
        assertThat(resourceManager.hasOpen()).as("has open").isTrue();
        assertThat(resourceManager.countOpen()).as("open count").isEqualTo(9);
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testOpenNonExisting(ResourceManager resourceManager) {
        assertThatThrownBy(() -> resourceManager.open(key("foo"))).as("openNonExisting")
                .isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @MethodSource("provideFilledListeningOpen")
    void testOpeningOrder(ResourceManager resourceManager, List<ResourceKey<?>> openingOrder) {
        resourceManager.open(key("movie"));

        assertThat(openingOrder)
                .containsSubsequence(key("shot"), key("movie"))
                .containsSubsequence(key("light"), key("shot"))
                .containsSubsequence(key("camera"), key("shot"))
                .containsSubsequence(key("cameraman"), key("shot"));
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testGetUnopened(ResourceManager resourceManager) {
        assertThat(resourceManager.get(key("light"))).as("light").isNull();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testGetOpened(ResourceManager resourceManager) {
        ResourceKey<TestResource> key = key("light");
        resourceManager.open(key);
        
        assertThat(resourceManager.get(key)).as("light").isInstanceOf(TestResource.class);
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testGetNonExisting(ResourceManager resourceManager) {
        assertThat(resourceManager.get(key("foo"))).as("light").isNull();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testOpenWithDirectException(ResourceManager resourceManager) {
        ResourceKey<TestResource> key = key("unopenable");
        resourceManager.register(key, ResourceManagerTest::createUnopenable);
        
        assertThatThrownBy(() -> resourceManager.open(key)).as("openingException")
                .isInstanceOf(OpeningFailedException.class)
                .extracting(e -> ((OpeningFailedException) e).getKey()).as("failedKey")
                .isEqualTo(key);
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testOpenWithDependencyException(ResourceManager resourceManager) {
        ResourceKey<TestResource> dependencyKey = key("unopenable");
        ResourceKey<TestResource> key = key("dependantOfUnopenable");
        resourceManager.register(dependencyKey, ResourceManagerTest::createUnopenable);
        resourceManager.register(key, ResourceManagerTest::create, dependencyKey);
        
        assertThatThrownBy(() -> resourceManager.open(key)).as("openingException")
                .isInstanceOf(OpeningFailedException.class)
                .extracting(e -> ((OpeningFailedException) e).getKey()).as("failedKey")
                .isEqualTo(dependencyKey);
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testSingleOpenAndClose(ResourceManager resourceManager) {
        ResourceKey<TestResource> key = key("light");
        TestResource resource = resourceManager.open(key);
        
        assertThat(!resource.isClosed()).as("really opened").isTrue();
        
        resourceManager.close(key);
        
        assertThat(resource.isClosed()).as("really closed").isTrue();
        
        assertThat(resourceManager.hasOpen()).as("has open").isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testComplexOpenAndSingleClose(ResourceManager resourceManager) {
        ResourceKey<?> key = key("movie");
        resourceManager.open(key);
        resourceManager.close(key);

        assertThat(resourceManager.openKeySet()).as("open keys").isEqualTo(keys(
                "light", "camera", "cameraman", "shot"));
        assertThat(!resourceManager.get(key("light")).isClosed()).as("light is on").isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testComplexOpenAndClose(ResourceManager resourceManager) {
        ResourceKey<?> movieKey = key("movie");
        ResourceKey<?> lightKey = key("light");
        resourceManager.open(movieKey);
        resourceManager.close(lightKey);

        assertThat(resourceManager.openKeySet()).as("open keys").isEqualTo(keys(
                "camera", "cameraman"));
    }

    @ParameterizedTest
    @MethodSource("provideFilledListeningClose")
    void testClosingOrder(ResourceManager resourceManager, List<ResourceKey<?>> closingOrder) {
        resourceManager.open(key("cookingshow"));

        resourceManager.close(key("cooking"));
        resourceManager.close(key("camera"));
        
        assertThat(closingOrder).isEqualTo(keyList("cookingshow", "cooking", "shot", "camera"));
    }

    @ParameterizedTest
    @MethodSource("provideEmpty")
    void testCloseAllOnEmptyWithoutException(ResourceManager resourceManager) {
        resourceManager.closeAll();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testCloseNonExisting(ResourceManager resourceManager) {
        assertThatThrownBy(() -> resourceManager.close(key("foo"))).as("closeNonExisting")
                .isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testCloseWithDirectException(ResourceManager resourceManager) {
        ResourceKey<TestResource> key = key("uncloseable");
        resourceManager.register(key, ResourceManagerTest::createUncloseable);
        resourceManager.open(key);
        
        assertThatThrownBy(() -> resourceManager.close(key)).as("closingException")
                .isInstanceOf(ClosingFailedException.class)
                .extracting(e -> ((ClosingFailedException) e).getKey()).as("failedKey")
                .isEqualTo(key);
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testCloseWithDependantException(ResourceManager resourceManager) {
        ResourceKey<TestResource> dependencyKey = key("dependency");
        ResourceKey<TestResource> uncloseableKey = key("uncloseable");
        ResourceKey<TestResource> leafDependantKey = key("leafDependant");
        resourceManager.register(dependencyKey, ResourceManagerTest::create);
        resourceManager.register(uncloseableKey, ResourceManagerTest::createUncloseable, dependencyKey);
        resourceManager.register(leafDependantKey, ResourceManagerTest::create, uncloseableKey);
        resourceManager.open(leafDependantKey);
        
        assertThatThrownBy(() -> resourceManager.close(dependencyKey)).as("closingException")
                .isInstanceOf(ClosingFailedException.class)
                .extracting(e -> ((ClosingFailedException) e).getKey()).as("failedKey")
                .isEqualTo(uncloseableKey);
        assertThat(resourceManager.openKeySet()).as("open keys").isEqualTo(
                new HashSet<>(Arrays.asList(dependencyKey, uncloseableKey)));
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testCloseAggressiveWithException(ResourceManager resourceManager) {
        ResourceKey<TestResource> dependencyKey = key("dependency");
        ResourceKey<TestResource> uncloseableKey = key("uncloseable");
        ResourceKey<TestResource> leafDependantKey = key("leafDependant");
        resourceManager.register(dependencyKey, ResourceManagerTest::create);
        resourceManager.register(uncloseableKey, ResourceManagerTest::createUncloseable, dependencyKey);
        resourceManager.register(leafDependantKey, ResourceManagerTest::create, uncloseableKey);
        resourceManager.open(leafDependantKey);
        
        assertThatThrownBy(() -> resourceManager.close(dependencyKey, true)).as("closingException")
                .isInstanceOf(ClosingFailedException.class)
                .extracting(e -> ((ClosingFailedException) e).getKey()).as("failedKey")
                .isEqualTo(uncloseableKey);
        assertThat(resourceManager.openKeySet()).as("open keys").isEqualTo(
                new HashSet<>(Arrays.asList(uncloseableKey)));
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testRemove(ResourceManager resourceManager) {
        ResourceKey<TestResource> key = key("light");
        resourceManager.remove(key);
        
        assertThat(resourceManager.containsKey(key)).as("contains light").isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testRemoveOpen(ResourceManager resourceManager) {
        ResourceKey<TestResource> key = key("light");
        TestResource resource = resourceManager.open(key);
        
        assertThat(!resource.isClosed()).as("light is really open").isTrue();
        
        resourceManager.remove(key);
        
        assertThat(resourceManager.containsKey(key)).as("contains light").isFalse();
        assertThat(resource.isClosed()).as("light is really closed").isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testRemoveWithException(ResourceManager resourceManager) {
        ResourceKey<TestResource> key = key("uncloseable");
        resourceManager.register(key, ResourceManagerTest::createUncloseable);

        resourceManager.open(key);
        
        assertThatThrownBy(() -> resourceManager.remove(key)).as("closing failed while removing")
                .isInstanceOf(ClosingFailedException.class);
        
        assertThat(resourceManager.containsKey(key)).as("contains uncloseable").isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testRemoveWithDependantException(ResourceManager resourceManager) {
        ResourceKey<TestResource> dependencyKey = key("dependency");
        ResourceKey<TestResource> uncloseableKey = key("uncloseable");
        resourceManager.register(dependencyKey, ResourceManagerTest::create);
        resourceManager.register(uncloseableKey, ResourceManagerTest::createUncloseable, dependencyKey);
        
        resourceManager.open(uncloseableKey);
        
        assertThatThrownBy(() -> resourceManager.remove(dependencyKey)).as("closing failed while removing")
                .isInstanceOf(ClosingFailedException.class)
                .extracting(exception -> ((ClosingFailedException) exception).getKey())
                .as("uncloseable key").isEqualTo(uncloseableKey);
        assertThat(resourceManager.get(dependencyKey).isClosed()).as("dependency is closed").isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testCloseAll(ResourceManager resourceManager) {
        ResourceKey<TestResource> testKey = key("shot");
        ResourceKey<TestResource> dependantKey = key("movie");
        resourceManager.open(dependantKey);
        
        assertThat(resourceManager.isOpen(testKey)).as("shot is open before").isTrue();

        resourceManager.closeAll();
        
        assertThat(resourceManager.isOpen(testKey)).as("shot is open after").isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testCloseAllWithException(ResourceManager resourceManager) {
        ResourceKey<TestResource> uncloseableKey = key("uncloseable");
        ResourceKey<TestResource> dependantKey = key("dependant");
        resourceManager.register(uncloseableKey, ResourceManagerTest::createUncloseable, key("movie"));
        resourceManager.register(dependantKey, ResourceManagerTest::create, key("uncloseable"));
        
        resourceManager.open(dependantKey);

        assertThatThrownBy(() -> resourceManager.closeAll()).as("close all exception")
                .isInstanceOf(ClosingFailedException.class)
                .extracting(exception -> ((ClosingFailedException) exception).getKey())
                .as("uncloseable key").isEqualTo(uncloseableKey);
        assertThat(resourceManager.get(key("light")).isClosed()).as("light is off").isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testCloseAllAggressiveWithException(ResourceManager resourceManager) {
        ResourceKey<TestResource> uncloseableKey = key("uncloseable");
        ResourceKey<TestResource> dependantKey = key("dependant");
        resourceManager.register(uncloseableKey, ResourceManagerTest::createUncloseable, key("movie"));
        resourceManager.register(dependantKey, ResourceManagerTest::create, key("uncloseable"));
        
        resourceManager.open(dependantKey);

        assertThatThrownBy(() -> resourceManager.closeAll(true)).as("closing all exception")
                .isInstanceOf(ClosingFailedException.class)
                .extracting(exception -> ((ClosingFailedException) exception).getKey())
                .as("uncloseable key").isEqualTo(uncloseableKey);
        assertThat(resourceManager.openKeySet()).as("open keys").isEqualTo(keys("uncloseable"));
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testClear(ResourceManager resourceManager) {
        resourceManager.clear();

        assertThat(resourceManager.isEmpty()).as("manager is empty").isTrue();
        assertThat(resourceManager.keySet()).as("keys").isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testClearWithOpen(ResourceManager resourceManager) {
        ResourceKey<TestResource> lightKey = key("light");
        TestResource light = resourceManager.open(lightKey);
        resourceManager.open(key("light"));
        resourceManager.clear();

        assertThat(resourceManager.isEmpty()).as("manager is empty").isTrue();
        assertThat(resourceManager.keySet()).as("keys").isEmpty();
        assertThat(light.isClosed()).as("light is off").isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideFilled")
    void testClearWithException(ResourceManager resourceManager) {
        ResourceKey<TestResource> key = key("uncloseable");
        resourceManager.register(key, ResourceManagerTest::createUncloseable);
        resourceManager.open(key);

        assertThatThrownBy(() -> resourceManager.clear()).as("clearing exception")
                .isInstanceOf(ClosingFailedException.class)
                .extracting(exception -> ((ClosingFailedException) exception).getKey())
                .as("uncloseable key").isEqualTo(key);
        assertThat(resourceManager.size()).as("size: all should be kept").isEqualTo(11);
    }

    public static Stream<ResourceManager> provideEmpty() {
        List<ResourceManager> instances = new ArrayList<>();
        for (Class<? extends ResourceManager> clazz : implementations.keySet()) {
            instances.add(buildInstance(clazz, ManagerContentKind.EMPTY, key -> {}, key -> {}));
        }
        return instances.stream();
    }

    public static Stream<ResourceManager> provideFilled() {
        List<ResourceManager> instances = new ArrayList<>();
        for (Class<? extends ResourceManager> clazz : implementations.keySet()) {
            instances.add(buildInstance(clazz, ManagerContentKind.DEFAULT, key -> {}, key -> {}));
        }
        return instances.stream();
    }
    
    public static Stream<Arguments> provideFilledListeningOpen() {
        List<Arguments> arguments = new ArrayList<>();
        for (Class<? extends ResourceManager> clazz : implementations.keySet()) {
            List<ResourceKey<?>> openingOrder = new ArrayList<>();
            ResourceManager resourceManager = buildInstance(
                    clazz, ManagerContentKind.DEFAULT, key -> openingOrder.add(key), key -> {});
            arguments.add(Arguments.of(resourceManager, openingOrder));
        }
        return arguments.stream();
    }

    public static Stream<Arguments> provideFilledListeningClose() {
        List<Arguments> arguments = new ArrayList<>();
        for (Class<? extends ResourceManager> clazz : implementations.keySet()) {
            List<ResourceKey<?>> closingOrder = new ArrayList<>();
            ResourceManager resourceManager = buildInstance(
                    clazz, ManagerContentKind.DEFAULT, key -> {}, key -> closingOrder.add(key));
            arguments.add(Arguments.of(resourceManager, closingOrder));
        }
        return arguments.stream();
    }
    
    private static ResourceManager buildInstance(
            Class<? extends ResourceManager> clazz,
            ManagerContentKind contentKind,
            Consumer<ResourceKey<?>> onOpen,
            Consumer<ResourceKey<?>> onClose) {
        
        ResourceFactory<TestResource> factory;
        factory = (manager, key) -> {
            onOpen.accept(key);
            return create();
        };
        ResourceCloser<TestResource> closer = (manager, key, value) -> {
            onClose.accept(key);
            value.close();
        };

        ResourceManager resourceManager = constructInstance(clazz, contentKind);
        fillInstance(contentKind, resourceManager, factory, closer);
        
        return resourceManager;
    }
    
    private static ResourceManager constructInstance(
            Class<? extends ResourceManager> clazz,
            ManagerContentKind contentKind) {

        String label = String.format(
                "%s - %s", clazz.getSimpleName(), contentKind.toString().toLowerCase());
        return implementations.get(clazz).apply(label);
    }
    
    private static void fillInstance(
            ManagerContentKind contentKind,
            ResourceManager resourceManager,
            ResourceFactory<TestResource> factory,
            ResourceCloser<TestResource> closer) {
        
        switch (contentKind) {
            case DEFAULT:
                resourceManager.register(key("light"), factory, closer);
                resourceManager.register(key("camera"), factory, closer);
                resourceManager.register(key("cameraman"), factory, closer);
                resourceManager.register(key("shot"), factory, closer, keys("light", "camera", "cameraman"));
                resourceManager.register(key("movie"), factory, closer, keys("shot"));
                resourceManager.register(key("fire"), factory, closer);
                resourceManager.register(key("cookbook"), factory, closer);
                resourceManager.register(key("chef"), factory, closer);
                resourceManager.register(key("cooking"), factory, closer, keys("light", "fire", "cookbook", "chef"));
                resourceManager.register(key("cookingshow"), factory, closer, keys("cooking", "shot"));
                break;
            case EMPTY:
                // nothing to do
        }
    }

    private static Set<ResourceKey<TestResource>> keys(String... names) {
        Set<ResourceKey<TestResource>> result = new HashSet<>();
        for (String name : names) {
            result.add(key(name));
        }
        return result;
    }

    private static List<ResourceKey<TestResource>> keyList(String... names) {
        List<ResourceKey<TestResource>> result = new ArrayList<>();
        for (String name : names) {
            result.add(key(name));
        }
        return result;
    }

    private static ResourceKey<TestResource> key(String name) {
        return new ResourceKey<>(name, TestResource.class);
    }

    private static TestResource create(
            ResourceManager resourceManager, ResourceKey<TestResource> key) {
        
        return create();
    }
    
    private static TestResource create() {
        return new TestResource();
    }

    private static TestResource createUnopenable(
            ResourceManager resourceManager, ResourceKey<TestResource> key) {
        
        return createUnopenable();
    }
    
    private static TestResource createUnopenable() {
        throw new IllegalStateException("This is unopenable!");
    }

    private static TestResource createUncloseable(
            ResourceManager resourceManager, ResourceKey<TestResource> key) {
        
        return createUncloseable();
    }

    private static TestResource createUncloseable() {
        return new TestResource(true);
    }
    
    
    private static class TestResource implements Closeable {
        
        private final boolean uncloseable;
        
        private boolean closed = false;
        
        
        TestResource() {
            this(false);
        }

        TestResource(boolean uncloseable) {
            this.uncloseable = uncloseable;
        }
        
        
        @Override
        public synchronized void close() throws IOException {
            if (uncloseable) {
                throw new IllegalStateException("I am uncloseable!");
            }
            closed = true;
        }
        
        public boolean isClosed() {
            return closed;
        }
        
    }
    
}
