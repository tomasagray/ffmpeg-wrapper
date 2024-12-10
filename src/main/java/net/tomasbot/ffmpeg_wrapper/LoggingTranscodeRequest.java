package net.tomasbot.ffmpeg_wrapper;

import java.util.function.Consumer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class LoggingTranscodeRequest extends TranscodeRequest {

  private boolean isLoggingEnabled;
  private Consumer<? super String> onEvent;
  private Consumer<? super Integer> onComplete;
  private Consumer<Throwable> onError;
}
