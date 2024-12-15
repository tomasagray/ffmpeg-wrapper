package net.tomasbot.ffmpeg_wrapper.request;

import java.nio.file.Path;
import java.util.function.Consumer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class LoggingTranscodeRequest extends TranscodeRequest {

  private boolean isLoggingEnabled;
  private Path logFile;

  private Consumer<? super String> onEvent;
  private Consumer<? super Integer> onComplete;
  private Consumer<Throwable> onError;
}
