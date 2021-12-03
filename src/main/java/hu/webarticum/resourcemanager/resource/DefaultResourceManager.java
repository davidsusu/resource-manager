package hu.webarticum.resourcemanager.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import hu.webarticum.resourcemanager.common.CastUtil;

/**
 * The default {@link ResourceManager} implementation
 */
public class DefaultResourceManager implements ResourceManager {

    private final String label;

    private final Map<ResourceKey<?>, Entry<?>> entries = new LinkedHashMap<>();

    private final Map<ResourceKey<?>, Set<ResourceKey<?>>> dependants = new HashMap<>();


    /**
     * Creates a new empty resource manager
     */
    public DefaultResourceManager() {
        this("Resources");
    }

    /**
     * Creates a new empty resource manager with the specified label
     *
     * @param label
     */
    public DefaultResourceManager(String label) {
        this.label = label;
    }


    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public synchronized <T> void register(
            ResourceKey<T> key,
            ResourceFactory<T> factory,
            ResourceCloser<T> closer,
            Collection<? extends ResourceKey<?>> dependencies) {
        
        if (entries.containsKey(key)) {
            throw new DuplicateKeyException(key);
        }
        checkCycle(key, dependencies);
        entries.put(key, new Entry<>(key, factory, closer, dependencies));
        for (ResourceKey<?> dependency : dependencies) {
            dependants.computeIfAbsent(dependency, k -> new HashSet<>()).add(key);
        }
    }

    @Override
    public synchronized boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public synchronized int size() {
        return entries.size();
    }

    @Override
    public synchronized Set<ResourceKey<?>> keySet() { // NOSONAR
        return new LinkedHashSet<>(entries.keySet());
    }

    @Override
    public synchronized boolean containsKey(ResourceKey<?> key) {
        return entries.containsKey(key);
    }

    @Override
    public synchronized <T> T get(ResourceKey<T> key) {
        Entry<T> entry = CastUtil.cast(entries.get(key));
        if (entry == null) {
            return null;
        }
        return entry.resource;
    }

    @Override
    public synchronized <T> T open(ResourceKey<T> key) {
        Entry<T> entry = getEntry(key);
        if (entry.resource != null) {
            return entry.resource;
        }
        for (ResourceKey<?> dependency : entry.dependecies) {
            open(dependency);
        }
        return entry.get();
    }

    @Override
    public synchronized void close(ResourceKey<?> key) {
        close(key, false);
    }

    @Override
    public synchronized void close(ResourceKey<?> key, boolean aggressive) {
        List<ClosingFailedException> exceptions = new ArrayList<>();
        for (ResourceKey<?> dependant : getDependants(key)) {
            try {
                close(dependant, aggressive);
            } catch (ClosingFailedException e) {
                 addOrThrow(exceptions, e, aggressive);
            }
        }
        try {
            getEntry(key).close();
        } catch (ClosingFailedException e) {
            addOrThrow(exceptions, e, aggressive);
        }
        throwIfAny(exceptions);
    }

    @Override
    public synchronized void closeAll() {
        closeAll(false);
    }

    @Override
    public synchronized void closeAll(boolean aggressive) {
        List<ClosingFailedException> exceptions = new ArrayList<>();
        for (ResourceKey<?> key : entries.keySet()) {
            try {
                close(key, aggressive);
            } catch (ClosingFailedException e) {
                addOrThrow(exceptions, e, aggressive);
            }
        }
        throwIfAny(exceptions);
    }

    private void addOrThrow(
            List<ClosingFailedException> exceptions,
            ClosingFailedException exception,
            boolean add) {
        
        if (add) {
            exceptions.add(exception);
        } else {
            throw exception;
        }
    }

    private void throwIfAny(List<ClosingFailedException> exceptions) {
        if (!exceptions.isEmpty()) {
            ClosingFailedException lastException = exceptions.remove(exceptions.size() - 1);
            for (ClosingFailedException exception : exceptions) {
                lastException.addSuppressed(exception);
            }
            throw lastException;
        }
    }

    @Override
    public synchronized <T> T remove(ResourceKey<T> key) {
        return remove(key, false);
    }

    @Override
    public synchronized <T> T remove(ResourceKey<T> key, boolean removeDependants) {
        close(key, false);

        T resource = getEntry(key).resource;

        if (removeDependants) {
            List<ResourceKey<?>> allDependants = getAllDependants(key);
            for (ResourceKey<?> dependant : allDependants) {
                entries.remove(dependant);
                dependants.remove(dependant);
            }
        }
        entries.remove(key);
        dependants.remove(key);

        return resource;
    }

    @Override
    public synchronized void clear() {
        closeAll();
        entries.clear();
        dependants.clear();
    }

    private List<ResourceKey<?>> getAllDependants(ResourceKey<?> key) {
        List<ResourceKey<?>> allDependants = new ArrayList<>();
        Set<ResourceKey<?>> currentDependants = getDependants(key);
        while (!currentDependants.isEmpty()) {
            allDependants.addAll(currentDependants);
            Set<ResourceKey<?>> newDependants = new HashSet<>();
            for (ResourceKey<?> currentKey : currentDependants) {
                newDependants.addAll(getDependants(currentKey));
            }
            newDependants.removeAll(allDependants);
            currentDependants = newDependants;
        }
        return allDependants;
    }

    private Set<ResourceKey<?>> getDependants(ResourceKey<?> key) {
        Set<ResourceKey<?>> result = dependants.get(key);
        return result == null ? new HashSet<>() : result;
    }

    @Override
    public synchronized boolean isOpen(ResourceKey<?> key) {
        return getEntry(key).resource != null;
    }

    @Override
    public synchronized boolean hasOpen() {
        for (Entry<?> entry : entries.values()) {
            if (entry.resource != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized int countOpen() {
        int result = 0;
        for (Entry<?> entry : entries.values()) {
            if (entry.resource != null) {
                result++;
            }
        }
        return result;
    }

    @Override
    public synchronized Set<ResourceKey<?>> openKeySet() { // NOSONAR
        Set<ResourceKey<?>> result = new LinkedHashSet<>();
        for (Entry<?> entry : entries.values()) {
            if (entry.resource != null) {
                result.add(entry.key);
            }
        }
        return result;
    }

    @Override
    public synchronized Set<ResourceKey<?>> closedKeySet() { // NOSONAR
        Set<ResourceKey<?>> result = new LinkedHashSet<>();
        for (Entry<?> entry : entries.values()) {
            if (entry.resource == null) {
                result.add(entry.key);
            }
        }
        return result;
    }

    private <T> Entry<T> getEntry(ResourceKey<T> key) {
        Entry<T> entry = CastUtil.cast(entries.get(key));
        if (entry == null) {
            throw new NoSuchElementException(String.format("Key not found: %s", key));
        }
        return entry;
    }

    private void checkCycle(ResourceKey<?> key, Collection<? extends ResourceKey<?>> dependencies) {
        Set<ResourceKey<?>> currentDependencies = new HashSet<>(dependencies);
        Set<ResourceKey<?>> allDependencies = new HashSet<>(dependencies);
        while (!currentDependencies.isEmpty()) {
            Set<ResourceKey<?>> nextDependencies = new HashSet<>();
            for (ResourceKey<?> dependency : currentDependencies) {
                if (dependency.equals(key)) {
                    throw new CyclicDependencyException();
                }
                Entry<?> entry = entries.get(key);
                if (entry != null) {
                    nextDependencies.addAll(entry.dependecies);
                }
            }
            nextDependencies.removeAll(allDependencies);
            allDependencies.addAll(nextDependencies);
            currentDependencies = nextDependencies;
        }
    }


    private class Entry<T> {

        final ResourceKey<T> key;

        final ResourceFactory<T> factory;

        final ResourceCloser<T> closer;

        final List<ResourceKey<?>> dependecies;

        T resource = null;


        Entry(
                ResourceKey<T> key,
                ResourceFactory<T> factory,
                ResourceCloser<T> closer,
                Collection<? extends ResourceKey<?>> dependecies) {
            
            this.key = key;
            this.factory = factory;
            this.closer = closer;
            this.dependecies = new ArrayList<>(dependecies);
        }


        T get() {
            if (resource == null) {
                try {
                    resource = factory.create(DefaultResourceManager.this, key);
                } catch (Exception e) {
                    throw new OpeningFailedException(key, e);
                }
            }
            return resource;
        }

        void close() {
            if (resource != null) {
                try {
                    closer.close(DefaultResourceManager.this, key, resource);
                } catch (Exception e) {
                    throw new ClosingFailedException(key, e);
                }
                resource = null;
            }
        }

    }

}
