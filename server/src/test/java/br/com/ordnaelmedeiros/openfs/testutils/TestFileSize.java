package br.com.ordnaelmedeiros.openfs.testutils;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TestFileSize(long bytes) {

  private static final Pattern NOTATION = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*([kmgt]?b?)$");

  private static final long KB = 1024L;
  private static final long MB = 1024L * KB;
  private static final long GB = 1024L * MB;
  private static final long TB = 1024L * GB;

  public static TestFileSize of(String notation) {
    if (notation == null) {
      throw new IllegalArgumentException("Size notation must not be null");
    }
    Matcher matcher = NOTATION.matcher(notation.trim().toLowerCase(Locale.ROOT));
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid size notation: " + notation);
    }
    double value = Double.parseDouble(matcher.group(1));
    long multiplier = switch (matcher.group(2)) {
      case "", "b" -> 1L;
      case "k", "kb" -> KB;
      case "m", "mb" -> MB;
      case "g", "gb" -> GB;
      case "t", "tb" -> TB;
      default -> throw new IllegalArgumentException("Invalid size notation: " + notation);
    };
    long parsedBytes = Math.round(value * multiplier);
    if (parsedBytes <= 0) {
      throw new IllegalArgumentException("Size must be positive: " + notation);
    }
    return new TestFileSize(parsedBytes);
  }

  public static TestFileSize ofMb(int sizeMb) {
    if (sizeMb <= 0) {
      throw new IllegalArgumentException("sizeMb must be positive: " + sizeMb);
    }
    return new TestFileSize(sizeMb * MB);
  }

  public String canonical() {
    if (bytes % TB == 0) {
      return bytes / TB + "tb";
    }
    if (bytes % GB == 0) {
      return bytes / GB + "gb";
    }
    if (bytes % MB == 0) {
      return bytes / MB + "mb";
    }
    if (bytes % KB == 0) {
      return bytes / KB + "kb";
    }
    return Long.toString(bytes);
  }

  @Override
  public String toString() {
    return canonical();
  }
}
