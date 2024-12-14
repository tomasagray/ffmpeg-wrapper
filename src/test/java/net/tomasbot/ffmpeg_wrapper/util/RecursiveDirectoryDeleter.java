package net.tomasbot.ffmpeg_wrapper.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.jetbrains.annotations.NotNull;

public final class RecursiveDirectoryDeleter extends SimpleFileVisitor<Path> {
  @Override
  public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs)
      throws IOException {
    Files.delete(file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc)
      throws IOException {
    Files.delete(dir);
    return FileVisitResult.CONTINUE;
  }
}
