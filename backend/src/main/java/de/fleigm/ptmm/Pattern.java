package de.fleigm.ptmm;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.routing.Observation;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A pattern combines all trips of a route that visit the same stations in the same sequence.
 * During shape generation instead of generating one shape per trip we generate only one per pattern.
 */
@Data
@Accessors(fluent = true)
public class Pattern {
  private final Route route;
  private final List<Stop> stops;
  private final List<Trip> trips;

  /**
   * @return ordered stops of this pattern as observations
   */
  public List<Observation> observations() {
    return stops.stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());
  }
}
