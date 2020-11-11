package de.fleigm.ptmm.http.json;

import io.quarkus.jsonb.JsonbConfigCustomizer;

import javax.inject.Singleton;
import javax.json.bind.JsonbConfig;

@Singleton
public class JsonbConfiguration implements JsonbConfigCustomizer {

  @Override
  public void customize(JsonbConfig config) {
    config.withSerializers(new PointListSerializer())
        .withSerializers(new LineStringSerializer())
        .withSerializers(new PathSerializer())
        .withSerializers(new EdgeIteratorStateSerializer())
        .withSerializers(new GHPointSerializer())
        .withSerializers(new ObservationSerializer())
        .withSerializers(new StateSerializer())
        .withSerializers(new ViewSerializer())
        .withPropertyVisibilityStrategy(new PrivateVisibilityStrategy());
  }
}