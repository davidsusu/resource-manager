package hu.webarticum.resourcemanager.resource;

/**
 * Exception, thrown when opening of a resource failed
 */
public class OpeningFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    
    private final ResourceKey<?> key;

    
    public OpeningFailedException(ResourceKey<?> key, Exception e) {
        super(String.format("Opening failed: %s", key), e);
        this.key = key;
    }

    
    /**
     * Gets the key of the unopenable resource
     * 
     * @return The key
     */
    public ResourceKey<?> getKey() { // NOSONAR
        return key;
    }
    
}