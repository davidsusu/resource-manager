package hu.webarticum.resourcemanager.resource;

/**
 * Simple {@link ResourceCloser} for {@link AutoCloseable} resources
 *
 * @param <T> Resource type
 */
public class AutoResourceCloser<T extends AutoCloseable> implements ResourceCloser<T> {

    @Override
    public void close(ResourceManager resourceManager, ResourceKey<T> key, T value) throws Exception {
        value.close();
    }

}
