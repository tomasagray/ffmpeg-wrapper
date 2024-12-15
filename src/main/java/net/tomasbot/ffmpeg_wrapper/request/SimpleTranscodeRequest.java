package net.tomasbot.ffmpeg_wrapper.request;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.tomasbot.ffmpeg_wrapper.metadata.FFmpegStream;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SimpleTranscodeRequest extends LoggingTranscodeRequest {

  @Builder
  public SimpleTranscodeRequest(
      URI from,
      Path to,
      String videoCodec,
      String audioCodec,
      List<FFmpegStream> streams,
      Map<String, Object> additionalArgs,
      boolean isLoggingEnabled,
      Path logFile,
      Consumer<? super String> onEvent,
      Consumer<? super Integer> onComplete,
      Consumer<Throwable> onError) {
    this.setFrom(List.of(from));
    this.setTo(to);
    this.setVideoCodec(videoCodec);
    this.setAudioCodec(audioCodec);
    this.setStreams(streams);
    this.setAdditionalArgs(additionalArgs);
    this.setLoggingEnabled(isLoggingEnabled);
    this.setLogFile(logFile);
    this.setOnEvent(onEvent);
    this.setOnError(onError);
    this.setOnComplete(onComplete);
  }
}
