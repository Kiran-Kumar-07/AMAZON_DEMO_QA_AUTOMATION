package com.ecommerce.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Loads config.properties once; any value can be overridden with a -D system property.
public final class ConfigReader {

    private static final Properties PROPERTIES = new Properties();
    private static final ConfigReader INSTANCE = new ConfigReader();

    private ConfigReader() {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (stream == null) {
                throw new IllegalStateException("config.properties not found on classpath (src/test/resources)");
            }
            PROPERTIES.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load config.properties", e);
        }
    }

    public static ConfigReader getInstance() {
        return INSTANCE;
    }

    public String get(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key));
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value == null || value.isBlank() ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    public String baseUrl() {
        return get("base.url");
    }

    public String browser() {
        return get("browser", "chrome");
    }

    public boolean headless() {
        return getBoolean("headless", false);
    }

    public int explicitWaitSeconds() {
        return getInt("explicit.wait", 20);
    }

    public int pageLoadTimeoutSeconds() {
        return getInt("page.load.timeout", 45);
    }

    public int productsToSelect() {
        return getInt("products.to.select", 5);
    }

    public String proofDir() {
        return get("proof.dir", "proof");
    }

    public String reportsDir() {
        return get("reports.dir", "reports");
    }

    public String testDataFile() {
        return get("test.data.file", "test-data/categories.csv");
    }

    public int categorySelectionRetry() {
        return getInt("category.selection.retry", 3);
    }
}
