package hu.webarticum.resourcemanager.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Utilities for loading {@link Properties} objects.
 */
public final class PropertiesUtil {

    private PropertiesUtil() {
    }


    /**
     * Loads properties from an internal resource with a default classloader.
     *
     * @param path The resource path.
     * @return The loaded properties.
     */
    public static Properties loadBundled(String path) {
        return loadBundled(PropertiesUtil.class.getClassLoader(), path);
    }

    /**
     * Loads properties from an internal resource with the given classloader.
     *
     * @param classLoader The classloader.
     * @param path The resource path.
     * @return The loaded properties.
     */
    public static Properties loadBundled(ClassLoader classLoader, String path) {
        Properties properties = new Properties();
        try {
            properties.load(classLoader.getResourceAsStream(path));
        } catch (NullPointerException | IOException e) {
            throw new NoClassDefFoundError("Configuration resource not found!");
        }
        return properties;
    }

    /**
     * Loads properties from a file.
     *
     * @param file The properties file.
     * @return The loaded properties.
     * @throws IOException
     */
    public static Properties loadFile(File file) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        }
        return properties;
    }

    /**
     * Loads properties from a URL.
     *
     * @param url The URL.
     * @return The loaded properties.
     * @throws IOException
     */
    public static Properties loadUrl(URL url) throws IOException {
        Properties properties = new Properties();
        try (InputStream inStream = url.openStream()) {
            properties.load(inStream);
        }
        return properties;
    }

    /**
     * Builds properties from the given keys and values.
     *
     * @param keysAndValues Keys and values alternately.
     * @return The built properties.
     */
    public static Properties buildFrom(String... keysAndValues) {
        Properties properties = new Properties();
        int entryCount = keysAndValues.length / 2;
        for (int i = 0; i < entryCount; i++) {
            int index = i * 2;
            String key = keysAndValues[index];
            String value = keysAndValues[index + 1];
            properties.put(key, value);
        }
        return properties;
    }

}
