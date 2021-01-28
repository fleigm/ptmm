package de.fleigm.ptmm.presets;

import com.conveyal.gtfs.GTFSFeed;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.events.Created;
import de.fleigm.ptmm.feeds.TransitFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GeneratePresetMetaInfo {
  private static final Logger logger = LoggerFactory.getLogger(GeneratePresetMetaInfo.class);

  @Inject
  TransitFeedService transitFeedService;

  @Inject
  PresetRepository presetRepository;

  public void run(@ObservesAsync @Created Preset preset) {
    TransitFeed transitFeed = transitFeedService.get(preset.getPath().resolve("gtfs.zip"));
    GTFSFeed feed = transitFeed.internal();

    Map<Integer, Long> routesPerType = transitFeed.routes().values().stream()
        .collect(Collectors.groupingBy(route -> route.route_type, Collectors.counting()));

    Map<Integer, Long> tripsPerType = transitFeed.trips().values().stream()
        .collect(Collectors.groupingBy(trip -> feed.routes.get(trip.route_id).route_type, Collectors.counting()));

    FeedDetails details = FeedDetails.builder()
        .info(feed.getFeedInfo())
        .agencies(new ArrayList<>(feed.agency.values()))
        .routes(feed.routes.size())
        .trips(feed.trips.size())
        .routesPerType(routesPerType)
        .tripsPerType(tripsPerType)
        .build();

    preset.addExtension(details);

    presetRepository.save(preset);

    logger.info("Generated meta info for preset {} - {}.", preset.getName(), preset.getId());
  }
}