package net.tomasbot.ffmpeg_wrapper.util;

import java.net.URI;
import org.jetbrains.annotations.NotNull;

public class Utilities {

  public static String getNormalizedPath(@NotNull URI uri) {
    if ("file".equals(uri.getScheme())) {
      return uri.getPath();
    }
    return uri.toString();
  }
}
