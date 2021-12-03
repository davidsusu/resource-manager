package hu.webarticum.resourcemanager.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Interface for storages of openable and closeable resources with basic dependency handling.
 *
 * When opening a resource, its dependencies will also be opened.
 * When closing a resource, its dependants will also be closed.
 */
public interface ResourceManager {

    /**
     * Gets the storage label
     *
     * @return The label
     */
    String getLabel();

    /**
     * Registers a new resource under the specified key
     *
     * @param key The key
     * @param factory The resource factory
     * @param dependencies Optional dependencies
     * @throws CyclicDependencyException is dependency cycle is detected
     */
    default <T extends AutoCloseable> void register(
            ResourceKey<T> key,
            ResourceFactory<T> factory,
            ResourceKey<?>... dependencies) {
        
        register(key, factory, new AutoResourceCloser<>(), Arrays.asList(dependencies));
    }

    /**
     * Registers a new resource under the specified key
     *
     * @param key The key
     * @param factory The resource factory
     * @param closer Custom closer code
     * @param dependencies Optional dependencies
     * @throws CyclicDependencyException is dependency cycle is detected
     */
    default <T> void register(
            ResourceKey<T> key,
            ResourceFactory<T> factory,
            ResourceCloser<T> closer,
            ResourceKey<?>... dependencies) {
        
        register(key, factory, closer, Arrays.asList(dependencies));
    }

    /**
     * Registers a new resource under the specified key
     *
     * @param key The key
     * @param factory The resource factory
     * @param dependencies Dependencies
     * @throws CyclicDependencyException is dependency cycle is detected
     */
    default <T extends AutoCloseable> void register(
            ResourceKey<T> key,
            ResourceFactory<T> factory,
            Collection<? extends ResourceKey<?>> dependencies) {
        
        register(key, factory, new AutoResourceCloser<>(), dependencies);
    }

    /**
     * Registers a new resource under the specified key
     *
     * @param key The key
     * @param factory The resource factory
     * @param closer Custom closer code
     * @param dependencies Dependencies
     * @throws CyclicDependencyException is dependency cycle is detected
     */
    <T> void register(
            ResourceKey<T> key,
            ResourceFactory<T> factory,
            ResourceCloser<T> closer,
            Collection<? extends ResourceKey<?>> dependencies);

    /**
     * Checks if this manager is empty
     *
     * @return <code>true</code> if there are no resource, <code>false</code> otherwise
     */
    boolean isEmpty();

    /**
     * Gets the number of resources in this manager
     *
     * @return The number of resources
     */
    int size();

    /**
     * Gets the set of registered keys
     *
     * @return The set of registered keys
     */
    Set<ResourceKey<?>> keySet(); // NOSONAR

    /**
     * Checks if the specified key is registered
     *
     * @param key The key
     * @return <code>true</code> if the key is registered, <code>false</code> otherwise
     */
    boolean containsKey(ResourceKey<?> key);

    /**
     * Gets resource under the specified key.
     *
     * Returns with null if <code>key</code> is not registered
     * or the resource is not open.
     *
     * @param key The key
     * @return The resource or <code>null</code>
     */
    <T> T get(ResourceKey<T> key);

    /**
     * Returns with open resource with the specified key.
     *
     * Opens the resource and its dependencies if it is not open.
     * Does not return with <code>null</code>. Throws exception if <code>key</code> is missing.
     *
     * @param key The key
     * @throws NoSuchElementException if <code>key</code> or any dependency is missing
     * @throws OpeningFailedException if opening of this resource or any dependency was failed
     * @return The open resource
     */
    <T> T open(ResourceKey<T> key);

    /**
     * Closes resource under the specified key.
     *
     * If <code>key</code> is missing, then exception will be thrown.
     * If resource is already open, no operation will be performed.
     * If closing was failed (because of this resource or any dependant),
     * throws an exception and stops closing,
     * so this close variant is non aggressive (see {@link #close(ResourceKey, boolean)}).
     *
     * @throws NoSuchElementException if <code>key</code> is missing
     * @throws ClosingFailedException if closing of this resource or any dependant was failed
     * @param key The key
     */
    void close(ResourceKey<?> key);

    /**
     * Closes resource under the specified key.
     *
     * If <code>key</code> is missing, then exception will be thrown.
     * If resource is already open, no operation will be performed.
     * If closing was failed (because of this resource or any dependant),
     * then throws an exception, and in aggressive mode does not stop and
     * tries to close each dependency (including this resource).
     *
     * @throws NoSuchElementException if <code>key</code> is missing
     * @throws ClosingFailedException if closing of this resource or any dependant was failed
     * @param key The key
     * @param aggressive Enables aggressive mode
     */
    void close(ResourceKey<?> key, boolean aggressive);

    /**
     * Closes all resources in this manager.
     *
     * This is non aggressive (see {@link #close(ResourceKey, boolean)}).
     *
     * @throws ClosingFailedException if closing of any resource was failed
     */
    void closeAll();

    /**
     * Closes all resources in this manager.
     *
     * This is possibly aggressive (see {@link #close(ResourceKey, boolean)}).
     *
     * @throws ClosingFailedException if closing of any resource was failed
     * @param aggressive Enables aggressive mode
     */
    void closeAll(boolean aggressive);

    /**
     * Closes and removes resource at the specified key.
     *
     * Dependants will kept, so calling this method can leave this manager
     * in a non consistent state, in which you can register an alternative
     * dependency instead of this. For instant consistency use {@link #remove(ResourceKey, boolean)}.
     * See {@link #close(ResourceKey, boolean)}. This is non aggressive,
     * and resource will be kept in case of a closing related exception.
     *
     * @param key The key
     * @throws NoSuchElementException if <code>key</code> is missing
     * @throws ClosingFailedException if closing of this resource or any dependant was failed
     * @return The resource removed from this key
     */
    <T> T remove(ResourceKey<T> key);

    /**
     * Closes and removes resource at the specified key and possibly its dependencies.
     *
     * See {@link #close(ResourceKey, boolean)}. This is non aggressive,
     * and no resource will be removed in case of a closing related exception.
     *
     * @param key The key
     * @param removeDependants Remove dependants too or not
     * @throws NoSuchElementException if <code>key</code> is missing
     * @throws ClosingFailedException if closing of this resource or any dependant was failed
     * @return The resource removed from this key
     */
    <T> T remove(ResourceKey<T> key, boolean removeDependants);

    /**
     * Closes and removes all resources.
     *
     * See {@link #closeAll()}.
     *
     * @throws ClosingFailedException if closing of any resource was failed
     */
    void clear();

    /**
     * Checks if resource under the specified key is registered and is open.
     *
     * @param key The key
     * @throws NoSuchElementException if key is missing
     * @return <code>true</code> if the resource is open, <code>false</code> otherwise
     */
    boolean isOpen(ResourceKey<?> key);

    /**
     * Checks if there is any open resource
     *
     * @return <code>true</code> is any open resource found, <code>false</code> otherwise
     */
    boolean hasOpen();

    /**
     * Gets the number of open resources in this manager
     *
     * @return The number of open resources
     */
    int countOpen();

    /**
     * Gets the set of keys of open resources in this manager
     *
     * @return The set of open keys
     */
    Set<ResourceKey<?>> openKeySet(); // NOSONAR

    /**
     * Gets the set of closed of open resources in this manager
     *
     * @return The set of closed keys
     */
    Set<ResourceKey<?>> closedKeySet(); // NOSONAR

}