package net.tomasbot.ffmpeg_wrapper;

import java.net.MalformedURLException;
import java.net.URL;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TestData {

  private static final String LARGE_VIDEO_URL =
      "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4";
  private static final String SMALL_VIDEO_URL =
      "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4";

  @Contract(" -> new")
  public static @NotNull URL getLargeVideoUrl() {
    return getUrl(LARGE_VIDEO_URL);
  }

  public static @NotNull URL getSmallVideoUr() {
    return getUrl(SMALL_VIDEO_URL);
  }

  private static @NotNull URL getUrl(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
