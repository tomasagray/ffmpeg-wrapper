package net.tomasbot.ffmpeg_wrapper.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tomasbot.ffmpeg_wrapper.request.TranscodeRequest;
import net.tomasbot.ffmpeg_wrapper.util.Utilities;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class FFmpegSingleStreamTask extends FFmpegStreamTask {

  private static final DateTimeFormatter LOGFILE_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");

  public FFmpegSingleStreamTask(String execCommand, TranscodeRequest request) {
    this.execCommand = execCommand;
    this.request = request;
  }

  @Override
  protected List<String> createExecCommand() {
    // Collate program arguments, format & return
    final List<String> arguments = new ArrayList<>();

    arguments.add(getExecCommand());
    arguments.addAll(request.getBaseArgs());
    arguments.addAll(getInputArgs().toList());
    arguments.addAll(getTranscodeArgs());
    arguments.add(request.getTo().toString());

    return arguments.stream().filter(Objects::nonNull).filter(s -> !s.isEmpty()).toList();
  }

  /**
   * Create directory to hold all streaming data
   *
   * @throws IOException If there are any problems with stream preparation
   */
  @Override
  protected void prepareStream() throws IOException {
    // Create output directory
    final Path outputDir = request.getTo().getParent();
    Files.createDirectories(outputDir);
    if (!outputDir.toFile().exists())
      throw new IOException("Could not create stream storage directory: " + outputDir);
  }

  @Override
  protected Stream<String> getInputArgs() {
    return request.getFrom().stream().flatMap(i -> Stream.of("-i", Utilities.getNormalizedPath(i)));
  }

  private @NotNull List<String> getTranscodeArgs() {
    List<String> args = new ArrayList<>();

    String videoCodec = request.getVideoCodec();
    if (videoCodec != null && !videoCodec.isEmpty()) {
      args.add("-c:v");
      args.add(videoCodec);
    }
    String audioCodec = request.getAudioCodec();
    if (audioCodec != null && !audioCodec.isEmpty()) {
      args.add("-c:a");
      args.add(audioCodec);
    }

    Map<String, Object> additionalArgs = request.getAdditionalArgs();
    if (additionalArgs != null) {
      additionalArgs.forEach(
          (key, value) -> {
            args.add(key);
            args.add(value.toString());
          });
    }

    // todo - map streams
    return args;
  }
}
