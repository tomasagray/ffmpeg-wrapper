package net.tomasbot.ffmpeg_wrapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class FFmpegStreamTask extends LoggableThread {

  private static final Logger logger = LogManager.getLogger(FFmpegStreamTask.class);

  private static final String LOG_FILENAME = "ffmpeg-%s.log";
  private static final OpenOption[] LOG_FILE_OPTS = {
    StandardOpenOption.CREATE, StandardOpenOption.WRITE
  };
  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");

  protected String execCommand;
  protected TranscodeRequest request;

  @Override
  public void run() {
    try {
      prepareStream();
      final List<String> commandArgs = createExecCommand();
      logger.info("Executing FFmpeg command: {}", commandArgs);

      this.process = new ProcessBuilder().command(commandArgs).start();

      if (loggingEnabled) {
        if (request instanceof LoggingTranscodeRequest loggingRequest) {
          this.onEvent = loggingRequest.getOnEvent();
          this.onError = loggingRequest.getOnError();
          this.onComplete = loggingRequest.getOnComplete();

          logStreamTask();
        } else
          throw new IllegalArgumentException(
              "Logging enabled but request was not a "
                  + LoggingTranscodeRequest.class.getSimpleName());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void logStreamTask() throws IOException {
    final Path logFile = getLogFile();
    final AsynchronousFileChannel fileChannel =
        AsynchronousFileChannel.open(logFile, LOG_FILE_OPTS);

    logPublisher =
        new FFmpegLogger()
            .beginLogging(this.process, fileChannel)
            .doOnNext(
                e -> {
                  if (onEvent != null) onEvent.accept(e);
                })
            .doOnError(
                e -> {
                  if (onError != null) onError.accept(e);
                })
            .doOnComplete(
                () -> {
                  if (process != null && onComplete != null)
                    process.onExit().thenAccept(p1 -> onComplete.accept(p1.exitValue()));
                });
    logPublisher.subscribe();
  }

  @NotNull
  private Path getLogFile() {
    final Path loggingDir = request.getTo().getParent();
    final String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    final String filename = String.format(LOG_FILENAME, timestamp);
    return loggingDir.resolve(filename);
  }

  /**
   * Forcefully halt execution of this task
   *
   * @return True/false if the task was successfully killed
   */
  public final boolean kill() {
    // Ensure process exists
    if (process != null) {
      ProcessHandle.allProcesses()
          .filter(p -> p.pid() == process.pid())
          .findFirst()
          .ifPresent(ProcessHandle::destroyForcibly);
      // Ensure process is dead
      return !process.isAlive();
    }
    return false;
  }

  /**
   * Get a formatted executable command (system CLI)
   *
   * @return The execution command
   */
  abstract List<String> createExecCommand();

  /**
   * Perform necessary preliminary setup tasks
   *
   * @throws IOException If there are any problems with stream preparation
   */
  abstract void prepareStream() throws IOException;

  /**
   * Returns a formatted String containing the input portion of the FFMPEG command
   *
   * @return The input portion of the FFMPEG command
   */
  abstract Stream<String> getInputArgs();
}
