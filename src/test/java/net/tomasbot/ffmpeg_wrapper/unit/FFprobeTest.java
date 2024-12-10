package net.tomasbot.ffmpeg_wrapper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import net.tomasbot.ffmpeg_wrapper.FFprobe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

@DisplayName("Validate FFprobe Wrapper")
class FFprobeTest {

  private static final Logger logger = LogManager.getLogger(FFprobeTest.class);

  private final String ffprobePath;

  FFprobeTest() {
    ffprobePath = System.getenv("FFPROBE_PATH");
    logger.info("FFprobe path is: {}", ffprobePath);
  }

  @Test
  @DisplayName("Check creation of FFprobe instance")
  void testFFprobeCreation() {
    // Create FFprobe instance
    logger.info("Instantiating FFprobe with executable: {}", ffprobePath);

    final FFprobe ffProbe = new FFprobe(ffprobePath);
    logger.info("Verifying FFprobe instance is NOT NULL...");

    assertThat(ffProbe).isNotNull();
  }

  @Test
  @DisplayName("Validate FFprobe version")
  void testFFprobeVersion() throws IOException {
    logger.info("Testing FFprobe version with executable: {} ...", ffprobePath);

    final FFprobe ffProbe = new FFprobe(ffprobePath);
    String version = ffProbe.getVersion();

    logger.info("FFprobe version: {}", version);
    assertThat(version).isNotNull().isNotEmpty();
  }
}
