package br.com.ordnaelmedeiros.openfs.testutils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TestFileFactoryTest {

  private static final long MIB = 1024L * 1024L;

  @Test
  void textContentFileWritesExactContent() throws Exception {
    Path file = TestFileFactory.textContentFile("hello openfs");
    assertTrue(file.getFileName().toString().startsWith("text-content-"));
    assertEquals("hello openfs", Files.readString(file));
  }

  @Test
  void textFileWithNotationWritesExactSize() throws Exception {
    Path file = TestFileFactory.textFile("1mb");
    assertEquals("text-1mb.txt", file.getFileName().toString());
    assertEquals(MIB, Files.size(file));
  }

  @Test
  void pngFileWithNotationApproximatesTargetSize() throws Exception {
    Path file = TestFileFactory.pngFile("1mb");
    assertEquals("image-1mb.png", file.getFileName().toString());
    long size = Files.size(file);
    long tolerance = MIB / 10;
    assertTrue(Math.abs(size - MIB) <= tolerance, "size " + size + " not within 10% of " + MIB);
  }

  @Test
  void filesWithSameSizeAreReused() {
    assertEquals(TestFileFactory.textFile("1mb"), TestFileFactory.textFile("1mb"));
    assertEquals(TestFileFactory.pngFile("1mb"), TestFileFactory.pngFile("1mb"));
    assertEquals(TestFileFactory.textFile("1mb"), TestFileFactory.textFile("1024kb"));
    assertEquals(TestFileFactory.textFile("1mb"), TestFileFactory.textFile(1));
  }

  @Test
  void textFileLargeNotationWritesExactSize() throws Exception {
    Path file = TestFileFactory.textFile("100mb");
    assertEquals("text-100mb.txt", file.getFileName().toString());
    assertEquals(100 * MIB, Files.size(file));
  }

  @Test
  void pngFileLargeNotationApproximatesTargetSize() throws Exception {
    Path file = TestFileFactory.pngFile("100mb");
    assertEquals("image-100mb.png", file.getFileName().toString());
    long target = 100 * MIB;
    long size = Files.size(file);
    assertTrue(Math.abs(size - target) <= target / 100, "size " + size + " not within 1% of " + target);
  }

  @Test
  void pngGenerationIsDeterministic() throws Exception {
    Path first = TestFileFactory.baseDir().resolve("deterministic-1.png");
    Path second = TestFileFactory.baseDir().resolve("deterministic-2.png");
    TestFileFactory.writePng(first, 32);
    TestFileFactory.writePng(second, 32);
    assertArrayEquals(Files.readAllBytes(first), Files.readAllBytes(second));
  }

  @Test
  void rejectsNonPositiveSize() {
    assertThrows(IllegalArgumentException.class, () -> TestFileFactory.textFile("0mb"));
    assertThrows(IllegalArgumentException.class, () -> TestFileFactory.pngFile("-1mb"));
    assertThrows(IllegalArgumentException.class, () -> TestFileFactory.textFile(0));
    assertThrows(IllegalArgumentException.class, () -> TestFileFactory.pngFile(-1));
  }
}
