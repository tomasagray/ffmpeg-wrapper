package net.tomasbot.ffmpeg_wrapper;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tomasbot.ffmpeg_wrapper.metadata.FFmpegStream;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConcatTranscodeRequest extends LoggingTranscodeRequest {

  @Builder
  ConcatTranscodeRequest(
      Collection<URI> from,
      Path to,
      String videoCodec,
      String audioCodec,
      List<FFmpegStream> streams,
      Map<String, Object> additionalArgs,
      boolean isLoggingEnabled,
      Consumer<? super String> onEvent,
      Consumer<? super Integer> onComplete,
      Consumer<Throwable> onError) {
    this.setFrom(new ArrayList<>(from));
    this.setTo(to);
    this.setVideoCodec(videoCodec);
    this.setAudioCodec(audioCodec);
    this.setStreams(streams);
    this.setAdditionalArgs(additionalArgs);
    this.setLoggingEnabled(isLoggingEnabled);
    this.setOnEvent(onEvent);
    this.setOnError(onError);
    this.setOnComplete(onComplete);
  }
}
