package de.fleigm.ptmm.cli;

import com.conveyal.gtfs.model.ShapePoint;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.matching.Observation;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.TransitRouter;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Collectors;

@Command(name = "generate", description = "generate missing shape files")
public class CreateShapeFileCommand implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(CreateShapeFileCommand.class);


  @Option(
      names = {"-t", "--temp"},
      paramLabel = "TEMP_FOLDER",
      description = "Folder for temporary files",
      defaultValue = "gh")
  String tempFolder;

  @Option(
      names = {"-c", "--clean"},
      paramLabel = "CLEAN",
      description = "Clean temporary files.")
  boolean clean;

  @Parameters(
      index = "0",
      paramLabel = "OSM_FILE",
      description = "OSM file")
  String osmFile;

  @Parameters(
      index = "1",
      paramLabel = "GTFS_FILE",
      description = "GTFS file")
  String gtfsFile;

  @Parameters(
      index = "2",
      paramLabel = "GENERATED_GTFS_FILE",
      description = "new GTFS file with generated shapes")
  String generatedGtfsFile;

  private TransitFeed feed;
  private GraphHopper graphHopper;
  private TransitRouter transitRouter;
  private ConcurrentNavigableMap<Fun.Tuple2<String, Integer>, ShapePoint> shapePoints;

  @Override
  public void run() {
    StopWatch stopWatch = new StopWatch();

    logger.info("Start initialization.");
    stopWatch.start();
    init();
    stopWatch.stop();
    logger.info("Finished initialization. ({} ms)", stopWatch.getMillis());

    logger.info("Start shape generation.");
    stopWatch.start();
    createShapes();
    stopWatch.stop();
    logger.info("Finished shape generation. ({} ms)", stopWatch.getMillis());

    logger.info("Writing new gtfs feed.");
    stopWatch.start();
    saveGtfsFeed();
    stopWatch.stop();
    logger.info("Finished new gtfs feed. ({} ms)", stopWatch.getMillis());
  }

  private void init() {
    graphHopper = loadGraphHopper(osmFile, tempFolder, clean);
    feed = new TransitFeed(gtfsFile);
    transitRouter = new TransitRouter(graphHopper, new PMap().putObject("profile", "bus_shortest"));
    this.shapePoints = feed.internal().shape_points;
  }

  private GraphHopper loadGraphHopper(String osmFile, String storagePath, boolean cleanTemporaryFiles) {
    if (cleanTemporaryFiles) {
      try {
        Files.deleteIfExists(Path.of(storagePath));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    PMap busFlagEncoderOptions = new PMap().putObject(com.graphhopper.util.Parameters.Routing.TURN_COSTS, true);

    GraphHopper graphHopper = new GraphHopperOSM()
        .forServer()
        .setGraphHopperLocation(storagePath)
        .setEncodingManager(EncodingManager.create(new BusFlagEncoder(busFlagEncoderOptions)))
        .setProfiles(
            new Profile("bus_fastest").setVehicle("bus").setWeighting("fastest").setTurnCosts(true),
            new Profile("bus_shortest").setVehicle("bus").setWeighting("shortest").setTurnCosts(true));

    graphHopper.setDataReaderFile(osmFile);

    graphHopper.importOrLoad();

    return graphHopper;
  }

  private void saveGtfsFeed() {
    feed.internal().toFile(generatedGtfsFile);
  }

  private void createShapes() {
    logger.info("Generate shapes for {} trips", feed.trips().size());
    StopWatch stopWatch = new StopWatch();
    LongSummaryStatistics statistics = new LongSummaryStatistics();

    stopWatch.start();

    feed.trips()
        .keySet()
        .parallelStream()
        .forEach(id -> createAndSetShapeForTrip(id, id));

    stopWatch.stop();
    statistics.accept(stopWatch.getMillis());

    /*for (String id : feed.trips().keySet()) {
      stopWatch.start();
      createAndSetShapeForTrip(id, id);
      stopWatch.stop();
      logger.info("Shape generation for trip {} took {}ms", id, stopWatch.getMillis());
      statistics.accept(stopWatch.getMillis());
    }*/

    logger.info("count: {} \ntotal duration: {}ms \navg duration: {}ms \nmin duration {}ms \nmax duration {}ms",
        statistics.getCount(),
        statistics.getSum(),
        statistics.getAverage(),
        statistics.getMin(),
        statistics.getMax());

  }

  private void createAndSetShapeForTrip(String shapeId, String tripId) {
    List<ShapePoint> shape = createShapeForTrip(shapeId, tripId);

    feed.trips().get(tripId).shape_id = shapeId;

    for (ShapePoint shapePoint : shape) {
      this.shapePoints.put(new Fun.Tuple2<>(shapeId, shapePoint.shape_pt_sequence), shapePoint);
    }
  }

  private List<ShapePoint> createShapeForTrip(String shapeId, String tripId) {
    List<Observation> observations = feed.getOrderedStopsForTrip(tripId)
        .stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());

    RoutingResult route = transitRouter.route(observations);

    PointList points = route.getPath().calcPoints();

    List<ShapePoint> shapePoints = new ArrayList<>(points.getSize());
    for (int i = 0; i < points.size(); i++) {
      shapePoints.add(new ShapePoint(shapeId, points.getLat(i), points.getLon(i), i, 0.0));
    }

    return shapePoints;

  }
}
