package by.honchar.hserver.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties loadProperties(final String filePath) throws IOException {
        final Properties properties = new Properties();
        try (final InputStream is = PropertiesLoader.class.getResourceAsStream(filePath)) {
            properties.load(is);
        }
        return properties;
    }

}
