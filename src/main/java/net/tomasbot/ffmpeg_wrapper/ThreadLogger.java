package net.tomasbot.ffmpeg_wrapper;

import java.nio.channels.AsynchronousFileChannel;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;

public interface ThreadLogger {

  Flux<String> beginLogging(@NotNull Process process, AsynchronousFileChannel fileChannel);
}
