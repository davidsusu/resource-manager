package hu.webarticum.resourcemanager.resource;

import java.io.Serializable;

/**
 * Key for resources to store under
 *
 * @param <T> Resource type
 */
public class ResourceKey<T> implements Serializable {

    private static final long serialVersionUID = 1L;


    private final String name;

    private final Class<T> resourceType;


    /**
     * Constructs a key.
     *
     * You can store resources with this key with the specified type only.
     * You can use the same <code>name</code> multiple times with different <code>type</code>s.
     *
     * @param name Name of this key
     * @param resourceType Resource type
     */
    public ResourceKey(String name, Class<T> resourceType) {
        this.name = name;
        this.resourceType = resourceType;
    }


    /**
     * Gets the key name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the associated resource type
     *
     * @return The resource type
     */
    public Class<T> getResourceType() {
        return resourceType;
    }

    /**
     * Checks if this key is equal to the given object.
     *
     * If <code>obj</code> is not a {@link ResourceKey}, then returns with false.
     * Thwo key are equal, iff their names and types are both equal.
     *
     * @return <code>true</code> if equals, <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ResourceKey)) {
            return false;
        }
        ResourceKey<?> otherKey = (ResourceKey<?>) obj;
        return name.equals(otherKey.name) && resourceType == otherKey.resourceType;
    }

    /**
     * Generates hash code for this key
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return (name.hashCode() * 31) + resourceType.hashCode();
    }

    /**
     * Generates string representation for this key.
     *
     * Format of the string is: &lt;name&gt; ":" &lt;type&gt; .
     *
     * @return The string representation
     */
    @Override
    public String toString() {
        return String.format("%s:%s", name, resourceType.getName());
    }

}