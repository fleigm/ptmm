package de.fleigm.ptmm.routing;

import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;

public class BusRouteNetwork {
  public static final String KEY = "bus_route_network";

  public static SimpleBooleanEncodedValue create() {
    return new SimpleBooleanEncodedValue(KEY, true);
  }
}
