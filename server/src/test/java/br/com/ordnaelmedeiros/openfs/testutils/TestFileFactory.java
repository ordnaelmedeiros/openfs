package br.com.ordnaelmedeiros.openfs.testutils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;

public final class TestFileFactory {

  private static final int BYTES_PER_PIXEL = 3;
  private static final String TEXT_PATTERN = "0123456789abcdef";
  private static final long NOISE_SEED = 42L;
  private static final int MAX_PNG_REFINEMENTS = 8;

  private static final Path BASE_DIR;
  private static final ConcurrentHashMap<TestFileSize, Path> TEXT_FILES_BY_SIZE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<TestFileSize, Path> IMAGE_FILES_BY_SIZE = new ConcurrentHashMap<>();
  private static final AtomicLong UNIQUE_SEQUENCE = new AtomicLong();

  static {
    ImageIO.setUseCache(false);
    try {
      BASE_DIR = Files.createTempDirectory("openfs-test-files-");
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create base directory for test files", e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteRecursively(BASE_DIR)));
  }

  private TestFileFactory() {
  }

  public static Path baseDir() {
    return BASE_DIR;
  }

  public static Path textContentFile(String content) {
    Path file = BASE_DIR.resolve("text-content-" + UNIQUE_SEQUENCE.incrementAndGet() + ".txt");
    try {
      Files.writeString(file, content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create text file: " + file, e);
    }
    return file;
  }

  public static Path textFile(String size) {
    return textFile(TestFileSize.of(size));
  }

  public static Path textFile(int sizeMb) {
    return textFile(TestFileSize.ofMb(sizeMb));
  }

  public static Path textFile(TestFileSize size) {
    return TEXT_FILES_BY_SIZE.computeIfAbsent(size, TestFileFactory::createTextFile);
  }

  public static Path pngFile(String size) {
    return pngFile(TestFileSize.of(size));
  }

  public static Path pngFile(int sizeMb) {
    return pngFile(TestFileSize.ofMb(sizeMb));
  }

  public static Path pngFile(TestFileSize size) {
    return IMAGE_FILES_BY_SIZE.computeIfAbsent(size, TestFileFactory::createPng);
  }

  private static Path createTextFile(TestFileSize size) {
    Path file = BASE_DIR.resolve("text-" + size.canonical() + ".txt");
    writeRepeatedText(file, size.bytes());
    return file;
  }

  private static void writeRepeatedText(Path file, long sizeBytes) {
    try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
      long remaining = sizeBytes;
      while (remaining > 0) {
        int chunk = (int) Math.min(TEXT_PATTERN.length(), remaining);
        writer.write(TEXT_PATTERN, 0, chunk);
        remaining -= chunk;
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create text file: " + file, e);
    }
  }

  private static Path createPng(TestFileSize size) {
    Path file = BASE_DIR.resolve("image-" + size.canonical() + ".png");
    long targetBytes = size.bytes();
    int side = Math.max(1, (int) Math.ceil(Math.sqrt((double) targetBytes / BYTES_PER_PIXEL)));
    int bestSide = side;
    int encodedSide = -1;
    long bestDiff = Long.MAX_VALUE;
    for (int attempt = 0; attempt <= MAX_PNG_REFINEMENTS; attempt++) {
      long encodedBytes = writePng(file, side);
      encodedSide = side;
      long diff = Math.abs(encodedBytes - targetBytes);
      if (diff < bestDiff) {
        bestDiff = diff;
        bestSide = side;
      }
      if (encodedBytes == targetBytes) {
        break;
      }
      int next = Math.max(1, (int) Math.round(side * Math.sqrt((double) targetBytes / encodedBytes)));
      if (next == side) {
        break;
      }
      side = next;
    }
    if (bestSide != encodedSide) {
      writePng(file, bestSide);
    }
    return file;
  }

  static long writePng(Path file, int side) {
    BufferedImage image = new BufferedImage(side, side, BufferedImage.TYPE_INT_RGB);
    int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    Random random = new Random(NOISE_SEED);
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = random.nextInt();
    }
    try {
      if (!ImageIO.write(image, "png", file.toFile())) {
        throw new IllegalStateException("No PNG writer available");
      }
      return Files.size(file);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create png file: " + file, e);
    }
  }

  private static void deleteRecursively(Path dir) {
    try (var stream = Files.walk(dir)) {
      stream.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
      });
    } catch (IOException ignored) {
    }
  }
}
