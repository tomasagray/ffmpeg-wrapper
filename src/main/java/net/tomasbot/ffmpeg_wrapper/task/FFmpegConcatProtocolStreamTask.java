package net.tomasbot.ffmpeg_wrapper.task;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tomasbot.ffmpeg_wrapper.request.TranscodeRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

@Data
@EqualsAndHashCode(callSuper = true)
public final class FFmpegConcatProtocolStreamTask extends FFmpegStreamTask {

    private final Path dataDir;

  public FFmpegConcatProtocolStreamTask(
      @NotNull String command, @NotNull TranscodeRequest request) {
        this.execCommand = command;
        if (Files.isDirectory(request.getTo())) {
            this.dataDir = request.getTo().getParent();
        } else {
            this.dataDir = request.getTo();
        }
    }

    @Override
    @Unmodifiable
    @NotNull
    protected List<String> createExecCommand() {
        // Collate program arguments, format & return
        final List<String> command = new ArrayList<>();
        command.add(this.getExecCommand());
        command.addAll(getInputArgs().toList());
        command.addAll(getArgumentList(request.getAdditionalArgs()));
        command.add(request.getTo().toString());
        return command;
    }

    private List<String> getArgumentList(@NotNull Map<String, Object> args) {
        return args.entrySet().stream()
                .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue().toString()))
                .toList();
    }

    @Override
    void prepareStream() throws IOException {
        // Create output directory
        Files.createDirectories(this.getDataDir());
    }

    @Override
    protected @NotNull Stream<String> getInputArgs() {
        // Concatenate URIs
        final String concatText =
                request.getFrom().stream().map(URI::toString).collect(Collectors.joining("|"));
        final String inputs = String.format("\"concat:%s\"", concatText);
        return Stream.of("-i", inputs);
    }
}
