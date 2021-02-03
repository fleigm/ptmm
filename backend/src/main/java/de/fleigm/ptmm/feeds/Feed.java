package de.fleigm.ptmm.feeds;

import de.fleigm.ptmm.util.Unzip;
import de.fleigm.ptmm.util.ValidateGtfsFeed;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class represent stored GTFS feed.
 * The feed is stored as zip (for {@link com.conveyal.gtfs.GTFSFeed})
 * and unzip (for {@link de.fleigm.ptmm.eval.process.Shapevl}).
 */
@Data
public class Feed {
  private final Path path;

  @JsonbCreator
  public Feed(@JsonbProperty("path") Path path) {
    this.path = path;
  }

  /**
   * Store zipped GTFS feed and unzip it.
   *
   * @param path zip file storage path (must include file name eg. gtfs.zip).
   * @param feed input stream of the zipped GTFS feed.
   * @return Feed.
   */
  public static Feed create(Path path, InputStream feed) {
    try {
      if (Files.exists(path)) {
        throw new IllegalArgumentException(String.format("Feed %s already exists.", path));
      }

      FileUtils.copyInputStreamToFile(feed, path.toFile());

      if (!ValidateGtfsFeed.validate(path)) {
        Files.delete(path);
        throw new IllegalArgumentException("File is not a valid GTFS feed.");
      }
      Unzip.apply(path, Path.of(FilenameUtils.removeExtension(path.toString())));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new Feed(path);
  }

  /**
   * Store a GTFS feed and unzip it.
   *
   * @param path        zip file storage path (must include file name eg. gtfs.zip).
   * @param transitFeed GTFS feed.
   * @return Feed.
   */
  public static Feed createFromTransitFeed(Path path, TransitFeed transitFeed) {
    try {
      transitFeed.internal().toFile(path.toString());
      Unzip.apply(path, Path.of(FilenameUtils.removeExtension(path.toString())));
      return new Feed(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return path to GTFS feed folder (for {@link de.fleigm.ptmm.eval.process.Shapevl}).
   */
  public Path getFolder() {
    return Path.of(FilenameUtils.removeExtension(path.toString()));
  }
}
