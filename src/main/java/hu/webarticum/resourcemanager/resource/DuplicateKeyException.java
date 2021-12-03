package hu.webarticum.resourcemanager.resource;

/**
 * Exception, thrown when a duplicated key given
 */
public class DuplicateKeyException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;


    private final ResourceKey<?> key;

    
    public DuplicateKeyException(ResourceKey<?> key) {
        super(String.format("Duplicate key: %s", key));
        this.key = key;
    }

    
    /**
     * Gets the key tried to duplicate
     * 
     * @return The key
     */
    public ResourceKey<?> getKey() { // NOSONAR
        return key;
    }
    
}