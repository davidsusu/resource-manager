package hu.webarticum.resourcemanager.resource;

/**
 * Exception thrown when closing of a resource failed
 */
public class ClosingFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    
    private final ResourceKey<?> key;

    
    public ClosingFailedException(ResourceKey<?> key, Exception e) {
        super(String.format("Closing failed: %s", key), e);
        this.key = key;
    }
    

    /**
     * Gets the key of the uncloseable resource
     * 
     * @return The key
     */
    public ResourceKey<?> getKey() { // NOSONAR
        return key;
    }
    
}