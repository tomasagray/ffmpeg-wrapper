package net.tomasbot.ffmpeg_wrapper.metadata;

import java.util.List;
import lombok.*;

/** Represents audio/video file metadata returned by FFPROBE */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode
public class FFmpegMetadata {

  private FFmpegFormat format;
  private List<FFmpegStream> streams;
  private List<FFmpegChapter> chapters;
}
