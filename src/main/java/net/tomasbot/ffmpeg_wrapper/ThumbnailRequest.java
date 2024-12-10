package net.tomasbot.ffmpeg_wrapper;

import java.nio.file.Path;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThumbnailRequest {

  private Path video;
  private Path thumbnail;
  private LocalTime at;
  private int width;
  private int height;
}
