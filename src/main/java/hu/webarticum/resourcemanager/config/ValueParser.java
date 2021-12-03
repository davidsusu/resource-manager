package hu.webarticum.resourcemanager.config;

/**
 * Interface for simple value parsers
 *
 * @param <T> Value type
 */
public interface ValueParser<T> {

    /**
     * Parses a textual value
     *
     * @param stringValue The textual value
     * @return The parsed item
     * @throws IllegalArgumentException If <code>stringValue</code> is invalid
     */
    T parse(String value);

}
