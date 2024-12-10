package net.tomasbot.ffmpeg_wrapper.unit;

import static net.tomasbot.ffmpeg_wrapper.TestData.FFMPEG_BASE_ARGS;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import net.tomasbot.ffmpeg_wrapper.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

@DisplayName("Validate FFmpeg Wrapper")
class FFmpegTest {

  private static final Logger logger = LogManager.getLogger(FFmpegTest.class);

  private final String ffmpegPath;

  FFmpegTest() {
    this.ffmpegPath = System.getenv("FFMPEG_PATH");
    logger.info("FFmpeg path is: {}", this.ffmpegPath);
  }

  @Test
  @DisplayName("Verify FFmpeg can be instantiated")
  void testFFmpegInstantiation() {
    logger.info("Instantiating FFmpeg with executable: {}", this.ffmpegPath);

    FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath, FFMPEG_BASE_ARGS);
    logger.info("FFmpeg is: {}", ffmpeg);

    assertThat(ffmpeg).isNotNull();
  }

  @Test
  @DisplayName("Validate FFmpeg version")
  void testFFmpegVersion() throws IOException {
    logger.info("Testing FFmpeg version of executable at: {}", this.ffmpegPath);

    FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath, FFMPEG_BASE_ARGS);
    String version = ffmpeg.getVersion();
    logger.info("FFmpeg version: {}", version);

    assertThat(version).isNotNull().isNotEmpty();
  }
}
