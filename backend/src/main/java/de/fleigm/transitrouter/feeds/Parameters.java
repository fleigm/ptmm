package de.fleigm.transitrouter.feeds;

import com.graphhopper.util.PMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Available parameters for shape generation of a GTFS feed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parameters {
  private String profile;
  private double candidateSearchRadius;
  private double sigma;
  private double beta;
  private boolean disableTurnCosts;
  private boolean useGraphHopperMapMatching;

  public static Parameters defaultParameters() {
    return Parameters.builder()
        .profile("bus_shortest")
        .candidateSearchRadius(25)
        .sigma(25)
        .beta(2.0)
        .disableTurnCosts(false)
        .build();
  }

  public PMap toPropertyMap() {
    return new PMap()
        .putObject("profile", profile)
        .putObject("measurement_error_sigma", sigma)
        .putObject("candidate_search_radius", candidateSearchRadius)
        .putObject("beta", beta)
        .putObject("disable_turn_costs", disableTurnCosts)
        .putObject("use_graph_hopper_map_matching", useGraphHopperMapMatching);
  }
}

