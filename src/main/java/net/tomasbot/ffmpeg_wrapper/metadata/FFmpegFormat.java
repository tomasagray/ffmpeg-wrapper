package net.tomasbot.ffmpeg_wrapper.metadata;

import java.util.Map;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class FFmpegFormat {

  private String filename;
  private int nb_streams;
  private int nb_programs;
  private String format_name;
  private String format_long_name;
  private double start_time;
  private double duration;
  private long size;
  private long bit_rate;
  private int probe_score;
  private Map<String, String> tags;
}
