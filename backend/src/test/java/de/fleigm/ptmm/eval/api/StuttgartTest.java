package de.fleigm.ptmm.eval.api;

import com.graphhopper.GraphHopper;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@QuarkusTest
public class StuttgartTest {

  @Inject
  EvaluationService evaluationService;

  @ConfigProperty(name = "user.home")
  String homeDir;

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  @Inject
  GraphHopper graphHopper;

  @Test
  void run_evaluation() throws IOException, ExecutionException, InterruptedException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, "st_complete").toFile());
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart.zip").toFile();

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("st_complete")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("bus_fastest_turn")
        .build();

    EvaluationResponse result = evaluationService.createEvaluation(request);

    result.process().get();

    assertTrue(result.process().isDone());
  }

  @ParameterizedTest
  @ValueSource(strings = {"bus_fastest", "bus_fastest_turn", "bus_shortest", "bus_shortest_turn"})
  void run_all_evaluations(String profile) throws IOException, ExecutionException, InterruptedException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, profile).toFile());
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart_bus_only.zip").toFile();

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name(profile)
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile(profile)
        .build();

    EvaluationResponse result = evaluationService.createEvaluation(request);

    result.process().get();

    assertTrue(result.process().isDone());
  }

  @ParameterizedTest
  @CsvSource({
      /*"25, 2.0", "20, 2.0", "15, 2.0", "10, 2.0", "5, 2.0", "2, 2.0",
      "25, 1.8", "20, 1.8", "15, 1.8", "10, 1.8", "5, 1.8", "2, 1.8",
      "25, 1.6", "20, 1.6", "15, 1.6", "10, 1.6", "5, 1.6", "2, 1.6",
      "25, 1.4", "20, 1.4", "15, 1.4", "10, 1.4", "5, 1.4", "2, 1.4",
      "25, 1.2", "20, 1.2", "15, 1.2", "10, 1.2", "5, 1.2", "2, 1.2",
      "25, 1.0", "20, 1.0", "15, 1.0", "10, 1.0", "5, 1.0", "2, 1.0",
      "25, 0.8", "20, 0.8", "15, 0.8", "10, 0.8", "5, 0.8", "2, 0.8",
      "25, 0.6", "20, 0.6", "15, 0.6", "10, 0.6", "5, 0.6", "2, 0.6",
      "25, 0.4", "20, 0.4", "15, 0.4", "10, 0.4", "5, 0.4", "2, 0.4",*/
      "25, 0.2", "20, 0.2", "15, 0.2", "10, 0.2", "5, 0.2", "2, 0.2"})
  void run_with_different_parameters(double sigma, double beta) throws IOException, ExecutionException, InterruptedException {
    File feed = Paths.get(homeDir, "/uni/bachelor/project/files/vg.zip").toFile();

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name(String.format("Victoria-Gasteiz_%.0f_%.1f", sigma, beta))
        .gtfsFeed(FileUtils.openInputStream(feed))
        .sigma(sigma)
        .candidateSearchRadius(sigma)
        .beta(beta)
        .profile("bus_fastest_turn")
        .build();

    EvaluationResponse result = evaluationService.createEvaluation(request);

    result.process().get();

    assertTrue(result.process().isDone());
    assertFalse(result.process().isCompletedExceptionally());
  }
}
