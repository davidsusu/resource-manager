package hu.webarticum.resourcemanager.resource;

/**
 * Exception, thrown when cyclic dependencies detected
 */
public class CyclicDependencyException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    
    // TODO: get information about the cycle
    public CyclicDependencyException() {
        super("Cyclic dependency detected");
    }
    
}