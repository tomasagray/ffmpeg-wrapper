package net.tomasbot.ffmpeg_wrapper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import net.tomasbot.ffmpeg_wrapper.FFprobe;
import net.tomasbot.ffmpeg_wrapper.ResourceFileReader;
import net.tomasbot.ffmpeg_wrapper.TestData;
import net.tomasbot.ffmpeg_wrapper.metadata.FFmpegMetadata;
import net.tomasbot.ffmpeg_wrapper.util.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Verify FFprobe Wrapper functionality for local & remote files")
public class FFprobeIntegrationTest {

  private static final Logger logger = LogManager.getLogger(FFprobeIntegrationTest.class);

  private static final String TEST_DATA_REMOTE = "ffprobe_test_data_remote.json";
  private static final String TEST_DATA_LOCAL = "ffprobe_test_data_local.json";
  private static final URL testUrl = TestData.getSmallVideoUr();

  private final String ffprobePath;

  public FFprobeIntegrationTest() {
    this.ffprobePath = System.getenv("FFPROBE_PATH");
  }

  @Test
  @DisplayName("Verify FFprobe can read remote file metadata")
  void testGetRemoteFileMetadata() throws URISyntaxException, IOException {
    // given
    final String expectedMetadata = ResourceFileReader.readTestFile(TEST_DATA_REMOTE);
    logger.info("Reading file data from: {}", testUrl);

    // when
    final FFprobe ffProbe = new FFprobe(ffprobePath);
    FFmpegMetadata actualMetadata = ffProbe.getFileMetadata(testUrl.toURI());

    // then
    logger.info("Testing metadata for correctness...");
    assertThat(actualMetadata).isNotNull();
    assertThat(JsonParser.toJson(actualMetadata)).isNotEmpty().isEqualTo(expectedMetadata);
  }

  @Test
  @DisplayName("Verify FFprobe can read file metadata from local disk")
  void testGetLocalFileMetadata() throws IOException, URISyntaxException {
    final String videoPath = "big_buck_bunny.mp4";

    // given
    final String expectedMetadata = ResourceFileReader.readTestFile(TEST_DATA_LOCAL);
    URL videoUrl = FFprobeIntegrationTest.class.getClassLoader().getResource(videoPath);
    assertThat(videoUrl).isNotNull();
    logger.info("Attempting to read metadata from local video file: {}", videoUrl);

    // when
    FFprobe ffprobe = new FFprobe(ffprobePath);
    FFmpegMetadata actualMetadata = ffprobe.getFileMetadata(videoUrl.toURI());
    assertThat(JsonParser.toJson(actualMetadata)).isNotEmpty().isEqualTo(expectedMetadata);
  }
}
