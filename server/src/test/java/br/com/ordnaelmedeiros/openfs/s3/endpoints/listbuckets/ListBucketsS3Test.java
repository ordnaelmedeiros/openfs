package br.com.ordnaelmedeiros.openfs.s3.endpoints.listbuckets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ordnaelmedeiros.openfs.testutils.BaseResourceTestS3;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsContainerExtension;
import br.com.ordnaelmedeiros.openfs.testutils.S3ClientFactory;
import br.com.ordnaelmedeiros.openfs.testutils.Target;
import br.com.ordnaelmedeiros.openfs.testutils.TargetProvider;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.List;
import java.util.UUID;

@QuarkusTest
@ExtendWith(OpenFsContainerExtension.class)
class ListBucketsS3Test extends BaseResourceTestS3 {

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListBucketsContainsCreatedBucketsInOrder(Target target) {
    String bucketA = "list-buckets-s3-a-" + UUID.randomUUID();
    String bucketB = "list-buckets-s3-b-" + UUID.randomUUID();

    try (S3Client s3 = S3ClientFactory.create(baseUrl(target))) {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucketA).build());
      s3.createBucket(CreateBucketRequest.builder().bucket(bucketB).build());

      ListBucketsResponse response = assertDoesNotThrow(() -> s3.listBuckets());

      List<String> names = response.buckets().stream().map(Bucket::name).toList();
      assertTrue(names.contains(bucketA), "Should contain bucket " + bucketA + ", got: " + names);
      assertTrue(names.contains(bucketB), "Should contain bucket " + bucketB + ", got: " + names);
      assertTrue(names.indexOf(bucketA) < names.indexOf(bucketB),
          "Buckets should be listed in alphabetical order, got: " + names);

      response.buckets().stream()
        .filter(bucket -> bucket.name().equals(bucketA) || bucket.name().equals(bucketB))
        .forEach(bucket -> assertNotNull(bucket.creationDate(),
            "CreationDate should be set for bucket " + bucket.name()));
    }
  }
}
