package hu.webarticum.resourcemanager.resource;

/**
 * Functional interface for resource factory lambdas
 *
 * @param <T> Resource type
 */
public interface ResourceFactory<T> {

    /**
     * Creates a resource for the given context
     * 
     * @param resourceManager The manager which stores the resource
     * @param key The resource key
     * @throws Exception in any case when creating or opening was failed
     */
    public T create(
            ResourceManager resourceManager, ResourceKey<T> key
            ) throws Exception; // NOSONAR
    
}