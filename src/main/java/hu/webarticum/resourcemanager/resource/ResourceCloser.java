package hu.webarticum.resourcemanager.resource;

/**
 * Functional interface for custom resource closer lambdas
 *
 * @param <T> Resource type
 */
public interface ResourceCloser<T> {

    /**
     * Perform closing the given resource in the given context
     *
     * @param resourceManager The manager which stores the resource
     * @param key The resource key
     * @param value The resource
     * @throws Exception in any case when closing was failed
     */
    public void close(
            ResourceManager resourceManager, ResourceKey<T> key, T value
            ) throws Exception; // NOSONAR

}