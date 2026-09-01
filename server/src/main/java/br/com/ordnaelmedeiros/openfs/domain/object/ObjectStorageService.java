package br.com.ordnaelmedeiros.openfs.domain.object;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import br.com.ordnaelmedeiros.openfs.domain.bucket.BucketStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

@ApplicationScoped
public class ObjectStorageService {

  public static final int DEFAULT_MAX_KEYS = 1000;

  public record ObjectListing(
    List<ObjectInfo> contents,
    List<String> commonPrefixes,
    int keyCount,
    int maxKeys,
    boolean isTruncated) {}

  public static class NoSuchBucketException extends RuntimeException {
    public NoSuchBucketException(String bucketName) {
      super("Bucket does not exist: " + bucketName);
    }
  }

  @Inject
  OpenFsConfig config;

  @Inject
  BucketStorageService bucketStorage;

  public ObjectListing listObjects(String bucketName, String prefix, String delimiter, Integer maxKeys) {
    if (!bucketStorage.bucketExists(bucketName)) {
      throw new NoSuchBucketException(bucketName);
    }
    Path bucketPath = bucketStorage.getBucketPath(bucketName);
    int limit = normalizeMaxKeys(maxKeys);
    try (Stream<Path> stream = Files.walk(bucketPath)) {
      TreeMap<String, ObjectInfo> entries = new TreeMap<>();
      stream
        .filter(Files::isRegularFile)
        .map(path -> toObjectInfo(bucketPath, path))
        .filter(info -> prefix == null || info.key().startsWith(prefix))
        .forEach(info -> addEntry(entries, info, prefix, delimiter));
      boolean truncated = entries.size() > limit;
      List<ObjectInfo> contents = new ArrayList<>();
      List<String> commonPrefixes = new ArrayList<>();
      int count = 0;
      for (var entry : entries.entrySet()) {
        if (count >= limit) {
          break;
        }
        if (entry.getValue() == null) {
          commonPrefixes.add(entry.getKey());
        } else {
          contents.add(entry.getValue());
        }
        count++;
      }
      return new ObjectListing(contents, commonPrefixes, count, limit, truncated);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to list objects in bucket: " + bucketName, e);
    }
  }

  private int normalizeMaxKeys(Integer maxKeys) {
    if (maxKeys == null || maxKeys <= 0) {
      return DEFAULT_MAX_KEYS;
    }
    return Math.min(maxKeys, DEFAULT_MAX_KEYS);
  }

  private void addEntry(TreeMap<String, ObjectInfo> entries, ObjectInfo info, String prefix, String delimiter) {
    if (delimiter != null && !delimiter.isEmpty()) {
      String base = prefix == null ? "" : prefix;
      String rest = info.key().substring(base.length());
      int index = rest.indexOf(delimiter);
      if (index >= 0) {
        entries.putIfAbsent(base + rest.substring(0, index + delimiter.length()), null);
        return;
      }
    }
    entries.put(info.key(), info);
  }

  private ObjectInfo toObjectInfo(Path bucketPath, Path path) {
    try {
      BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
      String key = bucketPath.relativize(path).toString().replace('\\', '/');
      return new ObjectInfo(key, attributes.size(), attributes.lastModifiedTime().toInstant());
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read attributes of: " + path, e);
    }
  }
}
