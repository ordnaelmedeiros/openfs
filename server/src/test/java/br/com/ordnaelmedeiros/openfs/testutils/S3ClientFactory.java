package br.com.ordnaelmedeiros.openfs.testutils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

public final class S3ClientFactory {

  private S3ClientFactory() {
  }

  public static S3Client create(String baseUrl) {
    return S3Client.builder()
      .endpointOverride(URI.create(baseUrl))
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
      .region(Region.US_EAST_1)
      .forcePathStyle(true)
      .build();
  }
}
