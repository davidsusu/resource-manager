package hu.webarticum.resourcemanager.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import hu.webarticum.resourcemanager.common.PropertiesUtil;

/**
 * Interface for supplying any properties
 */
public interface PropertiesSupplier {

    /**
     * Creates a supplier that loads properties from a file
     *
     * @param file The source file
     * @return The supplier
     */
    public static PropertiesSupplier of(File file) {
        return () -> PropertiesUtil.loadFile(file);
    }

    /**
     * Creates a supplier that loads properties from a URI
     *
     * @param uri The source URI
     * @return The supplier
     */
    public static PropertiesSupplier of(URI uri) {
        try {
            return of(uri.toURL());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }

    /**
     * Creates a supplier that loads properties from a URL
     *
     * @param url The source URL
     * @return The supplier
     */
    public static PropertiesSupplier of(URL url) {
        return () -> PropertiesUtil.loadUrl(url);
    }

    /**
     * Creates a supplier that loads properties from a bundled resource
     *
     * @param clazz Class to provide the context {@link ClassLoader}
     * @param path Path of the bundled resource
     * @return The supplier
     */
    public static PropertiesSupplier of(Class<?> clazz, String path) {
        return of(clazz.getClassLoader(), path);
    }

    /**
     * Creates a supplier that loads properties from a bundled resource
     *
     * @param classLoader Context {@link ClassLoader}
     * @param path Path of the bundled resource
     * @return The supplier
     */
    public static PropertiesSupplier of(ClassLoader classLoader, String path) {
        return () -> PropertiesUtil.loadBundled(classLoader, path);
    }


    /**
     * Loads properties from the specified source
     *
     * @return The property map
     * @throws Exception If any error occured
     */
    public Map<?, ?> get() throws Exception; // NOSONAR

}