package by.honchar.hserver.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClassPathFileLoader {

    public static Properties loadProperties(final String filePath) throws IOException {
        final Properties properties = new Properties();
        try (final InputStream is = ClassPathFileLoader.loadFileInputStream(filePath)) {
            properties.load(is);
        }
        return properties;
    }

    public static InputStream  loadFileInputStream(final String filePath) throws IOException {
        return ClassPathFileLoader.class.getClassLoader().getResourceAsStream(filePath);
    }

}
