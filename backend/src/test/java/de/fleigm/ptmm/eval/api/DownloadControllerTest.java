package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DownloadControllerTest {

  @Inject
  EvaluationService evaluationService;

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  private Info info;

  @BeforeEach
  void setup() throws IOException, ExecutionException, InterruptedException {
    if (info != null) {
      return;
    }

    FileUtils.deleteDirectory(Paths.get(evaluationFolder, "download_test").toFile());
    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("download_test")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .alpha(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .uTurnDistancePenalty(1500.0)
        .profile("bus_custom_shortest")
        .build();

    CompletableFuture<Info> evaluation = evaluationService.createEvaluation(request);

    info = evaluation.get();
  }


  @Test
  void can_download_generated_gtfs_feed() {
      given().when()
          .get("eval/download_test/download/generated")
          .then()
          .statusCode(200)
          .contentType(ContentType.BINARY);
  }

  @Test
  void can_download_all_files() throws IOException {
    Response response = given().when().get("eval/download_test/download");

    response.then()
        .statusCode(200)
        .contentType(ContentType.BINARY);

    ZipArchiveInputStream archive = new ZipArchiveInputStream(response.asInputStream());
    List<ZipArchiveEntry> entries = new ArrayList<>();
    ZipArchiveEntry entry;
    while ((entry = archive.getNextZipEntry()) != null ) {
      entries.add(entry);
    }

    List<String> fileNames = entries.stream()
        .map(ZipArchiveEntry::getName)
        .collect(Collectors.toList());

    assertTrue(fileNames.contains(Evaluation.GENERATED_GTFS_FEED));
    assertTrue(fileNames.contains(Evaluation.ORIGINAL_GTFS_FEED));
    assertTrue(fileNames.contains(Evaluation.INFO_FILE));
    assertTrue(fileNames.contains(Evaluation.GTFS_FULL_REPORT));
    assertTrue(fileNames.contains(Evaluation.SHAPEVL_OUTPUT));
  }

  @Test
  void return_404_if_evaluation_does_not_exist() {
      given().when()
          .get("eval/some_unknown_evaluation/download")
          .then()
          .statusCode(404);

    given().when()
        .get("eval/some_unknown_evaluation/download/generated")
        .then()
        .statusCode(404);
  }
}
