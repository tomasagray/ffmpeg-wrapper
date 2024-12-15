package net.tomasbot.ffmpeg_wrapper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.*;
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
import net.tomasbot.ffmpeg_wrapper.util.RecursiveDirectoryDeleter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Validate FFmpeg Wrapper transcode functionality")
public class FFmpegIntegrationTest {

  private static final Logger logger = LogManager.getLogger(FFmpegIntegrationTest.class);

  private static final String VIDEO_FILENAME = "big_buck_bunny.mp4";
  private static final String VIDEO_CODEC = "h264";
  private static final String AUDIO_CODEC = "aac";

  private static final Set<Path> cleanupDirs = new HashSet<>();

  private final String ffmpegPath;
  private final boolean isCleanupEnabled;

  FFmpegIntegrationTest() {
    this.ffmpegPath = System.getenv("FFMPEG_PATH");
    this.isCleanupEnabled = "true".equalsIgnoreCase(System.getenv("TEST_CLEANUP"));
  }

  @AfterAll
  static void cleanup() throws Exception {
    logger.info("Cleaning up test data ...");

    for (Path cleanupDir : cleanupDirs) {
      Files.walkFileTree(cleanupDir, new RecursiveDirectoryDeleter());

      if (!cleanupDir.toFile().exists())
        logger.info("Successfully cleaned up test data at: {}", cleanupDir);
      else throw new IOException("Could not clean up test data at: " + cleanupDir);
    }
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
    if (isCleanupEnabled) transcodeOutput.deleteOnExit();
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
    FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath, TestData.FFMPEG_BASE_ARGS);
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

    final FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath, TestData.FFMPEG_BASE_ARGS);
    final URL testVideoUrl = TestData.getLargeVideoUrl();
    final Path transcodeOutputDir = Files.createTempDirectory("FFMPEG_HLS_TEST_");
    final Path transcodeOutput = transcodeOutputDir.resolve("playlist.m3u8");

    final AtomicInteger exitCodeAccumulator = new AtomicInteger(-1);
    final SimpleTranscodeRequest transcodeRequest =
        SimpleTranscodeRequest.builder()
            .from(testVideoUrl.toURI())
            .to(transcodeOutput)
            .logFile(FFmpegStreamTask.getDefaultLogFile())
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

    if (isCleanupEnabled) cleanupDirs.add(transcodeOutputDir);
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
    final Path outputFile = dataDir.resolve("concat.mkv");

    final AtomicInteger exitCode = new AtomicInteger(-1);
    ConcatTranscodeRequest transcodeRequest =
        ConcatTranscodeRequest.builder()
            .from(from)
            .to(outputFile)
            .videoCodec(VIDEO_CODEC)
            .audioCodec(AUDIO_CODEC)
            .isLoggingEnabled(true)
            .logFile(FFmpegStreamTask.getDefaultLogFile())
            .onEvent(logger::info)
            .onError(logger::error)
            .onComplete(exitCode::lazySet)
            .build();

    // when
    final FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath, TestData.FFMPEG_BASE_ARGS);
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

    if (isCleanupEnabled) cleanupDirs.add(dataDir);
  }

  @Test
  @DisplayName("Validate thumbnail creation")
  void testThumbnailCreation() throws Exception {
    // given
    int expectedThumbWidth = 1280;
    int expectedThumbHeight = 720;
    int expectedFilesize = 77_147;

    final Path output = Files.createTempFile("FFMPEG_THUMBNAIL_TEST", ".jpg");
    if (isCleanupEnabled) output.toFile().deleteOnExit();
    final URL testVideoUrl = this.getClass().getClassLoader().getResource(VIDEO_FILENAME);
    assertThat(testVideoUrl).isNotNull();

    // when
    FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath, TestData.FFMPEG_BASE_ARGS);
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

  @Test
  @DisplayName("Validate ability to cancel a stream")
  void testKillStream() throws Exception {
    // given
    final int waitMs = 500;
    final int maximumOutputSize = 1_000;
    final int expectedExitCode = 137;

    final URL testVideoUrl = TestData.getLargeVideoUrl();
    final Path outputFile = Files.createTempFile("FFMPEG_KILL_STREAM_TEST_", ".mkv");
    if (isCleanupEnabled) outputFile.toFile().deleteOnExit();

    FFmpeg ffmpeg = new FFmpeg(this.ffmpegPath, TestData.FFMPEG_BASE_ARGS);
    AtomicInteger exitCode = new AtomicInteger(-1);
    SimpleTranscodeRequest transcodeRequest =
        SimpleTranscodeRequest.builder()
            .from(testVideoUrl.toURI())
            .to(outputFile)
            .onEvent(logger::info)
            .onError(logger::error)
            .onComplete(exitCode::lazySet)
            .build();

    // when
    logger.info("Streaming from: {} to: {}", testVideoUrl, outputFile);
    FFmpegStreamTask streamTask = ffmpeg.getTranscodeTask(transcodeRequest);
    streamTask.start(); // run task in background

    // ... wait...
    logger.info("Waiting {}ms ...", waitMs);
    TimeUnit.MILLISECONDS.sleep(waitMs);
    logger.info("Killing stream ...");
    final boolean killed = streamTask.kill();

    // then
    final int actualExitCode = exitCode.get();
    final long actualOutputSize = Files.size(outputFile);
    logger.info("Successfully killed stream? {}", killed);
    logger.info("Exit code: {}", actualExitCode);
    logger.info("Transcode output size: {} bytes", actualOutputSize);

    assertThat(killed).isTrue();
    assertThat(actualExitCode).isNotZero().isEqualTo(expectedExitCode);
    assertThat(actualOutputSize).isLessThan(maximumOutputSize);
  }
}
