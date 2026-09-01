package br.com.ordnaelmedeiros.openfs.s3.endpoints.listobjects;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsTestDataResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@QuarkusTest
@QuarkusTestResource(value = OpenFsTestDataResource.class, restrictToAnnotatedClass = true)
class ListObjectsQuarkusTest {

  @Inject
  OpenFsConfig config;

  private String baseUrl() {
    return "http://localhost:" + config.s3().port();
  }

  private Path createBucketWithData() throws IOException {
    String bucketName = "list-objects-quarkus-" + UUID.randomUUID();
    given()
      .when()
        .put(baseUrl() + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    Path bucketPath = Path.of(config.data().path()).resolve(bucketName);
    Files.writeString(bucketPath.resolve("a.txt"), "a");
    Files.writeString(bucketPath.resolve("b.txt"), "bb");
    Files.createDirectories(bucketPath.resolve("dir/sub"));
    Files.writeString(bucketPath.resolve("dir/c.txt"), "ccc");
    Files.writeString(bucketPath.resolve("dir/sub/d.txt"), "dddd");
    return bucketPath;
  }

  @Test
  void testListObjectsListsFilesRecursivelySortedWithMetadata() throws IOException {
    String bucketName = createBucketWithData().getFileName().toString();

    Response response = given()
      .when()
        .get(baseUrl() + "/" + bucketName + "?list-type=2")
      .then()
        .statusCode(200)
        .contentType(io.restassured.http.ContentType.XML)
        .extract().response();

    XmlPath xml = new XmlPath(response.asString());
    assertEquals(List.of("a.txt", "b.txt", "dir/c.txt", "dir/sub/d.txt"),
        xml.getList("ListBucketResult.Contents.Key", String.class));
    assertEquals(List.of("1", "2", "3", "4"),
        xml.getList("ListBucketResult.Contents.Size", String.class));
    assertEquals(List.of("STANDARD", "STANDARD", "STANDARD", "STANDARD"),
        xml.getList("ListBucketResult.Contents.StorageClass", String.class));
    List<String> lastModified = xml.getList("ListBucketResult.Contents.LastModified", String.class);
    assertEquals(4, lastModified.size());
    lastModified.forEach(date -> assertEquals(true, date != null && !date.isEmpty()));
    assertEquals(4, xml.getInt("ListBucketResult.KeyCount"));
    assertEquals(false, xml.getBoolean("ListBucketResult.IsTruncated"));
  }

  @Test
  void testListObjectsWithPrefixFiltersKeys() throws IOException {
    String bucketName = createBucketWithData().getFileName().toString();

    Response response = given()
      .when()
        .get(baseUrl() + "/" + bucketName + "?list-type=2&prefix=dir/")
      .then()
        .statusCode(200)
        .extract().response();

    XmlPath xml = new XmlPath(response.asString());
    assertEquals("dir/", xml.getString("ListBucketResult.Prefix"));
    assertEquals(List.of("dir/c.txt", "dir/sub/d.txt"),
        xml.getList("ListBucketResult.Contents.Key", String.class));
    assertEquals(2, xml.getInt("ListBucketResult.KeyCount"));
  }

  @Test
  void testListObjectsWithDelimiterGroupsCommonPrefixes() throws IOException {
    String bucketName = createBucketWithData().getFileName().toString();

    Response response = given()
      .when()
        .get(baseUrl() + "/" + bucketName + "?list-type=2&delimiter=/")
      .then()
        .statusCode(200)
        .extract().response();

    XmlPath xml = new XmlPath(response.asString());
    assertEquals("/", xml.getString("ListBucketResult.Delimiter"));
    assertEquals(List.of("a.txt", "b.txt"),
        xml.getList("ListBucketResult.Contents.Key", String.class));
    assertEquals(List.of("dir/"),
        xml.getList("ListBucketResult.CommonPrefixes.Prefix", String.class));
    assertEquals(3, xml.getInt("ListBucketResult.KeyCount"));
  }

  @Test
  void testListObjectsWithPrefixAndDelimiterGroupsSubPrefixes() throws IOException {
    String bucketName = createBucketWithData().getFileName().toString();

    Response response = given()
      .when()
        .get(baseUrl() + "/" + bucketName + "?list-type=2&prefix=dir/&delimiter=/")
      .then()
        .statusCode(200)
        .extract().response();

    XmlPath xml = new XmlPath(response.asString());
    assertEquals(List.of("dir/c.txt"),
        xml.getList("ListBucketResult.Contents.Key", String.class));
    assertEquals(List.of("dir/sub/"),
        xml.getList("ListBucketResult.CommonPrefixes.Prefix", String.class));
    assertEquals(2, xml.getInt("ListBucketResult.KeyCount"));
  }

  @Test
  void testListObjectsWithMaxKeysTruncates() throws IOException {
    String bucketName = createBucketWithData().getFileName().toString();

    Response response = given()
      .when()
        .get(baseUrl() + "/" + bucketName + "?list-type=2&max-keys=2")
      .then()
        .statusCode(200)
        .extract().response();

    XmlPath xml = new XmlPath(response.asString());
    assertEquals(List.of("a.txt", "b.txt"),
        xml.getList("ListBucketResult.Contents.Key", String.class));
    assertEquals(2, xml.getInt("ListBucketResult.KeyCount"));
    assertEquals(2, xml.getInt("ListBucketResult.MaxKeys"));
    assertEquals(true, xml.getBoolean("ListBucketResult.IsTruncated"));
  }

  @Test
  void testListObjectsV1XmlListsObjectsWithoutKeyCount() throws IOException {
    String bucketName = createBucketWithData().getFileName().toString();

    given()
      .when()
        .get(baseUrl() + "/" + bucketName)
      .then()
        .statusCode(200)
        .body(containsString("<Name>" + bucketName + "</Name>"))
        .body(containsString("<Key>dir/c.txt</Key>"))
        .body(not(containsString("<KeyCount>")));
  }
}
