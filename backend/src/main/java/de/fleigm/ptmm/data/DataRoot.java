package de.fleigm.ptmm.data;

import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import de.fleigm.ptmm.presets.PresetRepository;

public class DataRoot {
  private final GeneratedFeedRepository generatedFeeds;
  private final PresetRepository presets;

  private DataRoot() {
    generatedFeeds = new GeneratedFeedRepository(this);
    presets = new PresetRepository(this);
  }

  public static DataRoot create() {
    return new DataRoot();
  }

  public GeneratedFeedRepository generatedFeeds() {
    return generatedFeeds;
  }

  public PresetRepository presets() {
    return presets;
  }
}
