/*
 * Copyright (c) 2022.
 *
 * This file is part of Matchday.
 *
 * Matchday is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Matchday is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Matchday.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tomasbot.ffmpeg_wrapper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import net.tomasbot.ffmpeg_wrapper.FFprobe;
import net.tomasbot.ffmpeg_wrapper.ResourceFileReader;
import net.tomasbot.ffmpeg_wrapper.metadata.FFmpegMetadata;
import net.tomasbot.ffmpeg_wrapper.util.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FFprobeTest - Test the functionality of the FFprobe plugin")
class FFprobeTest {

  private static final Logger logger = LogManager.getLogger(FFprobeTest.class);

  private static final String TEST_DATA_REMOTE = "ffprobe_test_data_remote.json";
  private static final String TEST_DATA_LOCAL = "ffprobe_test_data_local.json";
  private static final URL testUrl;

  static {
    try {
      testUrl = new URL("https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

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
    URL videoUrl = FFprobeTest.class.getClassLoader().getResource(videoPath);
    assertThat(videoUrl).isNotNull();
    logger.info("Attempting to read metadata from local video file: {}", videoUrl);

    // when
    FFprobe ffprobe = new FFprobe(ffprobePath);
    FFmpegMetadata actualMetadata = ffprobe.getFileMetadata(videoUrl.toURI());
    assertThat(JsonParser.toJson(actualMetadata)).isNotEmpty().isEqualTo(expectedMetadata);
  }
}
