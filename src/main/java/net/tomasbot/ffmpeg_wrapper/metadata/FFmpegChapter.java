package net.tomasbot.ffmpeg_wrapper.metadata;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class FFmpegChapter {

  private String time_base;
  private long start;
  private String start_time;
  private long end;
  private String end_time;
}
