package net.tomasbot.ffmpeg_wrapper.task;

import java.nio.file.Path;
import java.util.function.Consumer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import reactor.core.publisher.Flux;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class LoggableThread extends Thread {

  protected Process process;

  protected boolean loggingEnabled = true;
  protected Path logFile;

  protected Flux<String> logPublisher;
  protected Consumer<? super String> onEvent;
  protected Consumer<? super Integer> onComplete;
  protected Consumer<Throwable> onError;

}
