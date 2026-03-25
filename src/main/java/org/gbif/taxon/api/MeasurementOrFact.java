package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * A property value of an accepted taxon.
 * Based on the Darwin Core MeasurementOrFact class: https://dwc.tdwg.org/terms/#measurementorfact
 */
@Data
@Schema(description = "A measurement, fact, characteristic, or assertion about a taxon. Based on Darwin Core MeasurementOrFact.")
public class MeasurementOrFact {

  @Schema(description = "The nature of the measurement, fact, characteristic, or assertion (dwc:measurementType). " +
    "Recommended best practice is to use a controlled vocabulary.",
    example = "tail length")
  private String measurementType;

  @Schema(description = "The value of the measurement, fact, characteristic, or assertion (dwc:measurementValue).",
    example = "45")
  private String measurementValue;

  @Schema(description = "Comments or notes accompanying the MeasurementOrFact (dwc:measurementRemarks).",
    example = "Measurement taken from preserved specimen")
  private String measurementRemarks;
}
