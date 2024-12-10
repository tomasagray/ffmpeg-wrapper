package net.tomasbot.ffmpeg_wrapper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.tomasbot.ffmpeg_wrapper.*;
import net.tomasbot.ffmpeg_wrapper.request.ConcatTranscodeRequest;
import net.tomasbot.ffmpeg_wrapper.request.SimpleTranscodeRequest;
import net.tomasbot.ffmpeg_wrapper.request.ThumbnailRequest;
import net.tomasbot.ffmpeg_wrapper.task.FFmpegStreamTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Validate FFmpeg Wrapper transcode functionality")
public class FFmpegIntegrationTest {

  private static final Logger logger = LogManager.getLogger(FFmpegIntegrationTest.class);

  private static final String VIDEO_FILENAME = "big_buck_bunny.mp4";
  private static final String VIDEO_CODEC = "h264";
  private static final String AUDIO_CODEC = "aac";

  private final String ffmpegPath;

  FFmpegIntegrationTest() {
    this.ffmpegPath = System.getenv("FFMPEG_PATH");
  }

  private static void runTestTask(Thread task) {
    logger.info("Running test task: {}", task);
    try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
      executor.submit(task);
    } finally {
      logger.info("Finished running test task: {}", task);
    }
  }

  @Test
  @DisplayName("Verify transcode of local video file")
  void testLocalTranscode() throws Exception {
    // given
    final long expectedFilesize = 1_292_643;
    final int expectedExitCode = 0;

    // from
    final URL testVideoUrl = this.getClass().getClassLoader().getResource(VIDEO_FILENAME);
    assertThat(testVideoUrl).isNotNull();
    // to
    final File transcodeOutput = File.createTempFile("FFMPEG_WRAPPER_TEST_", ".mkv");
    transcodeOutput.deleteOnExit();
    logger.info("Testing transcode of file: {} to: {}", testVideoUrl, transcodeOutput);

    final AtomicInteger actualExitCode = new AtomicInteger(-1);
    final SimpleTranscodeRequest transcodeRequest =
        SimpleTranscodeRequest.builder()
            .from(testVideoUrl.toURI())
            .to(transcodeOutput.toPath())
            .videoCodec(VIDEO_CODEC)
            .audioCodec(AUDIO_CODEC)
            .onEvent(logger::info)
            .onError(logger::error)
            .onComplete(actualExitCode::lazySet)
            .build();

    // when
    FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath);
    FFmpegStreamTask transcodeTask = ffmpeg.getTranscodeTask(transcodeRequest);
    runTestTask(transcodeTask);

    // then
    long outputFilesize = Files.size(transcodeOutput.toPath());
    logger.info("Output filesize: {}; expected: {}", outputFilesize, expectedFilesize);

    assertThat(transcodeOutput).exists();
    assertThat(outputFilesize).isNotZero().isEqualTo(expectedFilesize);
    assertThat(actualExitCode.get()).isEqualTo(expectedExitCode);
  }

  @Test
  @DisplayName("Verify transcode of HLS stream to local cache")
  void testHlsStream() throws Exception {
    // given
    final int expectedExitCode = 0;
    final int expectedSegmentCount = 47;

    final FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath);
    final URL testVideoUrl = TestData.getLargeVideoUrl();
    final Path transcodeOutputDir = Files.createTempDirectory("FFMPEG_HLS_TEST_");
    final Path transcodeOutput = transcodeOutputDir.resolve("playlist.m3u8");
    transcodeOutputDir.toFile().deleteOnExit();

    final AtomicInteger exitCodeAccumulator = new AtomicInteger(-1);
    final SimpleTranscodeRequest transcodeRequest =
        SimpleTranscodeRequest.builder()
            .from(testVideoUrl.toURI())
            .to(transcodeOutput)
            .onEvent(logger::info)
            .onError(logger::error)
            .onComplete(exitCodeAccumulator::lazySet)
            .build();

    // when
    logger.info("Starting HLS stream from: {} to: {}...", testVideoUrl, transcodeOutput);
    FFmpegStreamTask hlsStreamTask = ffmpeg.getHlsStreamTask(transcodeRequest);
    runTestTask(hlsStreamTask);

    // then
    String[] segmentList = transcodeOutputDir.toFile().list();
    assertThat(segmentList).isNotNull();
    int actualSegmentCount = segmentList.length;
    int actualExitCode = exitCodeAccumulator.get();
    logger.info("Transcode finished with exit code:  {}", actualExitCode);
    logger.info("Number of HLS files generated: {}", actualSegmentCount);

    assertThat(actualExitCode).isEqualTo(expectedExitCode);
    assertThat(actualSegmentCount).isNotZero().isEqualTo(expectedSegmentCount);
  }

  @Test
  @DisplayName("Validate concatenation of several streams into one")
  void testConcatStream() throws Exception {
    // given
    final int expectedExitCode = 0;
    final long expectedFilesize = 2_579_147;
    final URL testVideoUrl = this.getClass().getClassLoader().getResource(VIDEO_FILENAME);
    assertThat(testVideoUrl).isNotNull();

    final Collection<URI> from = List.of(testVideoUrl.toURI(), testVideoUrl.toURI());
    final Path dataDir = Files.createTempDirectory("FFMPEG_CONCAT_TEST_");
    dataDir.toFile().deleteOnExit();
    final Path outputFile = dataDir.resolve("concat.mkv");

    final AtomicInteger exitCode = new AtomicInteger(-1);
    ConcatTranscodeRequest transcodeRequest =
        ConcatTranscodeRequest.builder()
            .from(from)
            .to(outputFile)
            .videoCodec(VIDEO_CODEC)
            .audioCodec(AUDIO_CODEC)
            .isLoggingEnabled(true)
            .onEvent(logger::info)
            .onError(logger::error)
            .onComplete(exitCode::lazySet)
            .build();

    // when
    final FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath);
    FFmpegStreamTask transcodeTask = ffmpeg.getTranscodeTask(transcodeRequest);
    logger.info("Starting concat transcode of: {} to: {}", from, outputFile);
    runTestTask(transcodeTask);

    // give stream a chance to finish
    TimeUnit.SECONDS.sleep(1);

    // then
    int actualExitCode = exitCode.get();
    long actualFilesize = Files.size(outputFile);

    logger.info("Transcode completed with exit code: {}", actualExitCode);
    logger.info("Output filesize: {}", actualFilesize);
    assertThat(actualExitCode).isEqualTo(expectedExitCode);
    assertThat(actualFilesize).isNotZero().isEqualTo(expectedFilesize);
  }

  @Test
  @DisplayName("Validate thumbnail creation")
  void testThumbnailCreation() throws Exception {
    // given
    int expectedThumbWidth = 1280;
    int expectedThumbHeight = 720;
    int expectedFilesize = 77_147;

    final Path output = Files.createTempFile("FFMPEG_THUMBNAIL_TEST", ".jpg");
    output.toFile().deleteOnExit();
    final URL testVideoUrl = this.getClass().getClassLoader().getResource(VIDEO_FILENAME);
    assertThat(testVideoUrl).isNotNull();

    // when
    FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath);
    ThumbnailRequest request =
        ThumbnailRequest.builder()
            .video(Path.of(testVideoUrl.toURI()))
            .thumbnail(output)
            .at(LocalTime.of(0, 0, 2))
            .width(expectedThumbWidth)
            .height(expectedThumbHeight)
            .build();
    Path thumbnail = ffmpeg.createThumbnail(request);
    logger.info("Created thumbnail at: {}", thumbnail);

    assertThat(thumbnail).isNotNull();
    assertThat(thumbnail.toFile()).exists();
    long actualFilesize = Files.size(thumbnail);
    logger.info("Thumbnail filesize: {}", actualFilesize);
    assertThat(actualFilesize).isEqualTo(expectedFilesize);

    // then
    BufferedImage image = ImageIO.read(output.toFile());
    int actualWidth = image.getWidth();
    int actualHeight = image.getHeight();
    logger.info("Image dimensions: {}w, {}h", actualWidth, actualHeight);
    assertThat(actualWidth).isEqualTo(expectedThumbWidth);
    assertThat(actualHeight).isEqualTo(expectedThumbHeight);
  }
}