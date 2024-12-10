package net.tomasbot.ffmpeg_wrapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TestData {

  public static final List<String> FFMPEG_BASE_ARGS =
      List.of("-v", "info", "-y", "-protocol_whitelist", "concat,file,http,https,tcp,tls,crypto");
  public static final List<String> FFPROBE_BASE_ARGS =
      List.of(
          "-hide_banner",
          "-print_format",
          "json",
          "-show_streams",
          "-show_format",
          "-show_chapters");

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
