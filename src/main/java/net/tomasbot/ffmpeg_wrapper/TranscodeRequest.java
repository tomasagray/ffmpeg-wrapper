package net.tomasbot.ffmpeg_wrapper;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.Data;
import net.tomasbot.ffmpeg_wrapper.metadata.FFmpegStream;

@Data
public abstract class TranscodeRequest {

  private List<String> baseArgs;
  private List<URI> from;
  private Path to;
  private String videoCodec;
  private String audioCodec;
  private List<FFmpegStream> streams;
  private Map<String, Object> additionalArgs;
}
