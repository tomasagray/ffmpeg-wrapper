package net.tomasbot.ffmpeg_wrapper.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class PropertiesLoader {

  private static final String PROPERTIES_FILE = "ffmpeg-wrapper.properties";

  private static Properties properties;

  private static @NotNull Properties loadProperties() throws IOException {
    Properties properties = new Properties();
    InputStream inputStream =
        PropertiesLoader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
    if (inputStream == null)
      throw new IOException("Could not find application properties file: " + PROPERTIES_FILE);
    properties.load(inputStream);
    inputStream.close();
    return properties;
  }

  public static Properties getProperties() throws IOException {
    if (properties == null) {
      properties = loadProperties();
    }

    return properties;
  }

  public static String getProperty(String key) {
    try {
      return getProperties().getProperty(key);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
