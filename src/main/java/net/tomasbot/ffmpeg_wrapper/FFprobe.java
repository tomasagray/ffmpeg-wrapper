package net.tomasbot.ffmpeg_wrapper;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import net.tomasbot.ffmpeg_wrapper.metadata.FFmpegMetadata;
import net.tomasbot.ffmpeg_wrapper.util.JsonParser;
import net.tomasbot.ffmpeg_wrapper.util.ProcessOutputReader;
import net.tomasbot.ffmpeg_wrapper.util.Utilities;
import org.jetbrains.annotations.NotNull;

public class FFprobe extends FFexecutable {

  private final List<String> baseArgs;

  public FFprobe(@NotNull String execPath, @NotNull List<String> baseArgs) {
    this.execPath = execPath;
    this.baseArgs = baseArgs;
  }

  /**
   * Read audio/video file metadata from a URI
   *
   * @param uri The URI of the video file
   * @throws IOException If there is an error reading data
   */
  private @NotNull String readFileMetadata(@NotNull final URI uri) throws IOException {
    // Assemble args for this job
    final List<String> processArgs = new ArrayList<>(this.baseArgs);
    processArgs.add(0, this.execPath);

    // Add remote URL to job args
    final String normalizedPath = Utilities.getNormalizedPath(uri);
    processArgs.add(normalizedPath);

    final Process process = new ProcessBuilder().command(processArgs).start();
    return ProcessOutputReader.readProcessData(process);
  }

  /**
   * Retrieve metadata from an audio/video file
   *
   * @param uri The file resource pointer
   * @return The file metadata
   * @throws IOException If the metadata could not be read or parsed
   */
  public FFmpegMetadata getFileMetadata(@NotNull final URI uri) throws IOException {
    return JsonParser.fromJson(readFileMetadata(uri), FFmpegMetadata.class);
  }
}
