package br.com.ordnaelmedeiros.openfs.testutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TestFileSizeTest {

  private static final long KB = 1024L;
  private static final long MB = 1024L * KB;
  private static final long GB = 1024L * MB;

  @Test
  void parsesSupportedNotations() {
    assertEquals(1, TestFileSize.of("1b").bytes());
    assertEquals(512 * KB, TestFileSize.of("512kb").bytes());
    assertEquals(2 * MB, TestFileSize.of("2mb").bytes());
    assertEquals(5 * GB, TestFileSize.of("5gb").bytes());
  }

  @Test
  void parsingIsCaseInsensitiveAndToleratesSpaces() {
    assertEquals(TestFileSize.of("2mb"), TestFileSize.of("2MB"));
    assertEquals(TestFileSize.of("2mb"), TestFileSize.of(" 2 MB "));
    assertEquals(TestFileSize.of("2mb"), TestFileSize.of("2m"));
  }

  @Test
  void parsesDecimalValues() {
    assertEquals(1572864, TestFileSize.of("1.5mb").bytes());
    assertEquals(1536 * KB, TestFileSize.of("1.5mb").bytes());
  }

  @Test
  void plainNumberMeansBytes() {
    assertEquals(1024, TestFileSize.of("1024").bytes());
  }

  @Test
  void canonicalUsesLargestUnit() {
    assertEquals("2mb", TestFileSize.of("2048kb").canonical());
    assertEquals("5gb", TestFileSize.of("5gb").canonical());
    assertEquals("1536kb", TestFileSize.of("1.5mb").canonical());
    assertEquals("1kb", TestFileSize.of("1024b").canonical());
  }

  @Test
  void rejectsInvalidNotations() {
    assertThrows(IllegalArgumentException.class, () -> TestFileSize.of("abc"));
    assertThrows(IllegalArgumentException.class, () -> TestFileSize.of("10zb"));
    assertThrows(IllegalArgumentException.class, () -> TestFileSize.of("0mb"));
    assertThrows(IllegalArgumentException.class, () -> TestFileSize.of("-1mb"));
    assertThrows(IllegalArgumentException.class, () -> TestFileSize.of(null));
  }
}
