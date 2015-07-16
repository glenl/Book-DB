package com.vulturegraphics.glbd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration details for glbd application
 */
class Config {
    private static String CONFIG_PROPERTIES = "/glbd.properties";
    private Properties properties;

    private Config() {
        properties = new Properties();
        try {
            InputStream is = getClass().getResourceAsStream(CONFIG_PROPERTIES);
            if (is != null) {
                properties.load(is);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Config getInstance() {
        return ConfigHolder.INSTANCE;
    }

    public String getProperty(String prop) {
        return properties.getProperty(prop);
    }

    public void dump() {
        properties.list(System.out);
    }

    private static class ConfigHolder {
        private static final Config INSTANCE = new Config();
    }
}
