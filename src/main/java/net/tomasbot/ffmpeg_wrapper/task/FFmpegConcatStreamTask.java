package net.tomasbot.ffmpeg_wrapper.task;

import java.io.IOException;
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

/** Class which creates an FFMPEG concatenation task; concatenates multiple video files into one */
@Data
@EqualsAndHashCode(callSuper = true)
public final class FFmpegConcatStreamTask extends FFmpegStreamTask {

    private static final String CONCAT_FILENAME = "concat.txt";

    private Path concatFile;
  private final Path dataDir;

  public FFmpegConcatStreamTask(@NotNull String command, @NotNull TranscodeRequest request) {
        this.execCommand = command;
        this.request = request;
    this.dataDir = this.request.getTo().getParent();
    }

    @Override
    @Unmodifiable
    @NotNull
    protected List<String> createExecCommand() {
        final List<String> command = new ArrayList<>();

        command.add(this.execCommand);
    command.addAll(this.request.getBaseArgs());
        command.addAll(this.getInputArgs().toList());
    Map<String, Object> additionalArgs = request.getAdditionalArgs();
    if (additionalArgs != null) {
      command.addAll(this.getArgumentList(additionalArgs));
    }
        command.add(request.getTo().toString());

        return command;
    }

    private List<String> getArgumentList(@NotNull Map<String, Object> args) {
        return args.entrySet().stream()
                .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue().toString()))
                .toList();
    }

    @Override
    protected void prepareStream() throws IOException {
    // Create output directory
    Files.createDirectories(dataDir);

        // Create URI list text file
        this.concatFile = createConcatFile();
    if (!this.concatFile.toFile().exists()) {
      throw new IOException("Could not create concat file: " + this.concatFile);
    }
    }

    @Override
    public @NotNull Stream<String> getInputArgs() {
        return Stream.of("-f", "concat", "-safe", "0", "-i", this.concatFile.toString());
    }

    /**
     * Create the text file (concat.txt) used by FFMPEG for concatenation
     *
     * @return The path of the concat.txt file
     * @throws IOException If there is an error creating or writing the file
     */
    private Path createConcatFile() throws IOException {
    // Map each URI to en entry in the concat file
    final String concatFileText =
        this.request.getFrom().stream()
            .map(url -> String.format("file '%s'", url))
            .collect(Collectors.joining("\n"));

    // Write data to file
    final Path concatFilePath = this.dataDir.resolve(CONCAT_FILENAME);
        return Files.writeString(concatFilePath, concatFileText);
    }
}
