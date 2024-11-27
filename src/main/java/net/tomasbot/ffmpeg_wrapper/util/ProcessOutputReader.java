package net.tomasbot.ffmpeg_wrapper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ProcessOutputReader {

  public static @NotNull String readProcessData(@NotNull Process process) throws IOException {
    try (InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      // Read data and collect as a String
      return reader.lines().collect(Collectors.joining(""));
    } finally {
      // Ensure process closed
      process.destroy();
    }
  }
}
