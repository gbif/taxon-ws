package org.gbif.species.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * A property value of an accepted taxon.
 */
@Data
@Schema(description = "A taxon property as key, value pair")
public class MeasurementOrFact {
  private String measurementType;
  private String measurementValue;
  private String measurementRemarks;
}
