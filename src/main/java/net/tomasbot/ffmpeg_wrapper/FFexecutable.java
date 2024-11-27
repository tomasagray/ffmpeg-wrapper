package net.tomasbot.ffmpeg_wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.tomasbot.ffmpeg_wrapper.util.ProcessOutputReader;
import net.tomasbot.ffmpeg_wrapper.util.PropertiesLoader;
import org.jetbrains.annotations.NotNull;

public abstract class FFexecutable {

  private Pattern versionPattern;
  protected String execPath;

  protected @NotNull Pattern getVersionPattern() {
    if (versionPattern == null) {
      final String pattern = PropertiesLoader.getProperty("version-pattern");
      versionPattern = Pattern.compile(pattern);
    }

    return versionPattern;
  }

  public String getVersion() throws IOException {
    final List<String> args = new ArrayList<>();
    args.add(this.execPath);
    args.add("-version");

    Process process = new ProcessBuilder().command(args).start();
    String output = ProcessOutputReader.readProcessData(process);
    Matcher matcher = getVersionPattern().matcher(output);

    if (matcher.find()) return matcher.group(1);
    // else...
    throw new IOException("Could not determine version of executable: " + this.execPath);
  }
}
