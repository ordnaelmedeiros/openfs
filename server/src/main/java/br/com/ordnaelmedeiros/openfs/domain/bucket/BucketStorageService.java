package br.com.ordnaelmedeiros.openfs.domain.bucket;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ApplicationScoped
public class BucketStorageService {

  // Bucket name rules:
  // - 3-63 characters total
  // - Only lowercase letters (a-z), numbers (0-9), hyphens (-) and dots (.)
  // - Must start and end with a lowercase letter or number
  // - Cannot start or end with hyphen or dot
  // - Prevents path traversal (no .., /, etc.)
  private static final Pattern BUCKET_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$");

  @Inject
  OpenFsConfig config;

  public void createBucket(String bucketName) throws IOException {
    validateBucketName(bucketName);
    Path bucketPath = getBucketPath(bucketName);
    Files.createDirectories(bucketPath);
    Log.debugf("Bucket created: %s", bucketPath);
  }

  public boolean bucketExists(String bucketName) {
    validateBucketName(bucketName);
    Path bucketPath = getBucketPath(bucketName);
    return Files.isDirectory(bucketPath);
  }

  public List<BucketInfo> listBuckets() throws IOException {
    Path root = Path.of(config.data().path());
    if (!Files.isDirectory(root)) {
      return List.of();
    }
    try (Stream<Path> stream = Files.list(root)) {
      return stream
        .filter(Files::isDirectory)
        .map(path -> path.getFileName().toString())
        .filter(name -> BUCKET_NAME_PATTERN.matcher(name).matches())
        .sorted()
        .map(this::toBucketInfo)
        .flatMap(Optional::stream)
        .toList();
    }
  }

  private Optional<BucketInfo> toBucketInfo(String bucketName) {
    Path bucketPath = getBucketPath(bucketName);
    try {
      BasicFileAttributes attributes = Files.readAttributes(bucketPath, BasicFileAttributes.class);
      return Optional.of(new BucketInfo(bucketName, attributes.creationTime().toInstant()));
    } catch (IOException e) {
      Log.warnf(e, "Failed to read attributes for bucket: %s", bucketName);
      return Optional.empty();
    }
  }

  private void validateBucketName(String bucketName) {
    if (bucketName == null || !BUCKET_NAME_PATTERN.matcher(bucketName).matches()) {
      throw new IllegalArgumentException("Invalid bucket name. Must be 3-63 characters, lowercase letters, numbers, hyphens, or dots. Cannot start/end with hyphen.");
    }
  }

  private Path getBucketPath(String bucketName) {
    return Path.of(config.data().path()).resolve(bucketName);
  }
}
