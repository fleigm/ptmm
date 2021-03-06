package de.fleigm.transitrouter.presets;

import de.fleigm.transitrouter.data.Entity;
import de.fleigm.transitrouter.data.Extensions;
import de.fleigm.transitrouter.data.HasExtensions;
import de.fleigm.transitrouter.gtfs.Feed;
import lombok.*;

import java.nio.file.Path;

/**
 * Preset of a GTFS feed that allow the generation and comparison of new GTFS feeds.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Preset extends Entity implements HasExtensions {
  private String name;
  private Feed feed;

  @Builder.Default
  private Extensions extensions = new Extensions();

  @Override
  public Extensions extensions() {
    return extensions;
  }

  @Override
  protected Path entityStorageRoot() {
    return storageRoot().resolve("presets");
  }
}
