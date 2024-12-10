# ffmpeg-wrapper

ffmpeg-wrapper is a lightweight Java frontend for ffmpeg/ffprobe. It is aimed at simplifying video and audio transcoding
within Java.

## Usage

To use `ffmpeg-wrapper`, first instantiate FFmpeg thus:

    List<String> baseArgs = List.of("-v", "info");
    FFmpeg ffmpeg = new FFmpeg("/path/to/ffmpeg/executable", baseArgs);

Create a transcode request, using the appropriate builder:

    SimpleTranscodeRequest transcodeRequest =
      SimpleTranscodeRequest.builder()
        .from(testVideoUrl.toURI())
        .to(transcodeOutput.toPath())
        .videoCodec(VIDEO_CODEC)
        .audioCodec(AUDIO_CODEC)
        .onEvent(logger::info)
        .onError(logger::error)
        .onComplete(i -> logger.info("We're done! Exit code: {}", i))
        .build();

Then submit the request to ffmpeg:

    FFmpegStreamTask streamTask = ffmpeg.getTranscodeTask(transcodeRequest);
