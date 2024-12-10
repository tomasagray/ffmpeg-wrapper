package net.tomasbot.ffmpeg_wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class FFmpeg extends FFexecutable {

  private static final String SEGMENT_PATTERN = "segment_%05d.ts";

  private final List<String> baseArgs;

  @Getter @Setter boolean loggingEnabled = true;

  public FFmpeg(@NotNull final String execPath) {
    this.execPath = execPath;
    // TODO: extract base args
    this.baseArgs =
        List.of("-v", "info", "-y", "-protocol_whitelist", "concat,file,http,https,tcp,tls,crypto");
  }

  public FFmpegStreamTask getHlsStreamTask(@NotNull TranscodeRequest request) {
    setBaseArgs(request);
    request.setAdditionalArgs(getHlsArgs(request.getTo()));
    return new FFmpegSingleStreamTask(execPath, request);
  }

  public FFmpegStreamTask getTranscodeTask(@NotNull TranscodeRequest request) {
    setBaseArgs(request);
    return request.getFrom().size() > 1
        ? new FFmpegConcatStreamTask(execPath, request)
        : new FFmpegSingleStreamTask(execPath, request);
  }

  private void setBaseArgs(@NotNull TranscodeRequest request) {
    List<String> args = new ArrayList<>(baseArgs);
    request.setBaseArgs(args);
  }

  private @NotNull Map<String, Object> getHlsArgs(@NotNull final Path storageLocation) {
    final Map<String, Object> transcodeArgs = new LinkedHashMap<>();

    final Path absoluteStorageLocation =
        Files.isDirectory(storageLocation)
            ? storageLocation.toAbsolutePath()
            : storageLocation.toAbsolutePath().getParent();
    final Path segmentPattern = absoluteStorageLocation.resolve(SEGMENT_PATTERN);

    // Add arguments
    transcodeArgs.put("-vcodec", "copy");
    transcodeArgs.put("-acodec", "copy");
    transcodeArgs.put("-muxdelay", "0");
    transcodeArgs.put("-f", "hls");
    transcodeArgs.put("-hls_playlist_type", "event");
    transcodeArgs.put("-hls_segment_filename", segmentPattern);

    return transcodeArgs;
  }

  public Path createThumbnail(@NotNull ThumbnailRequest request) throws IOException {
    List<String> args =
        marshallThumbArgs(
            request.getVideo(),
            request.getThumbnail(),
            request.getAt(),
            request.getWidth(),
            request.getHeight());
    Process process = new ProcessBuilder().command(args).start();

    try (InputStreamReader in = new InputStreamReader(process.getErrorStream());
        BufferedReader reader = new BufferedReader(in)) {
      reader.lines().forEach(System.out::println);
    }

    return request.getThumbnail();
  }

  @NotNull
  private ArrayList<String> marshallThumbArgs(
      @NotNull Path video, @NotNull Path thumb, @NotNull LocalTime time, int w, int h) {
    final ArrayList<String> args = new ArrayList<>(baseArgs);
    args.add(0, this.execPath);
    args.add("-loglevel");
    args.add("error");
    args.add("-ss");
    args.add(time.toString());
    args.add("-i");
    args.add(video.toString());
    args.add("-vf");
    args.add(String.format("scale=%d:%d", w, h));
    args.add("-vframes");
    args.add("1");
    args.add(thumb.toString());
    return args;
  }

  @Override
  public String toString() {
    return this.execPath;
  }
}
