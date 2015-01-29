package glbd;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class Config {
    private static String CONFIG_PROPERTIES = "/glbd.properties";
    private Properties properties;

    private Config() {
        properties = new Properties();
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(CONFIG_PROPERTIES);
            if (is != null) {
                properties.load(is);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
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

    public static Config getInstance() {
        return ConfigHolder.INSTANCE;
    }
}
