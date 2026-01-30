package org.gbif.species.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Geographic distribution information for a taxon.
 */
@Data
@Schema(description = "Geographic distribution information for a taxon")
public class Distribution {

  @Schema(description = "The identifier for the location", example = "loc123")
  private String locationID;

  @Schema(description = "The name or description of the locality", example = "Central European Mountains")
  private String locality;

  @Schema(description = "ISO 3166-1 alpha-2 country code", example = "DE")
  private String countryCode;

  @Schema(description = "The life stage to which the distribution applies", example = "adult")
  private String lifeStage;

  @Schema(description = "The establishment means (native, introduced, etc.)", example = "native")
  private String establishmentMeans;

  @Schema(description = "The degree of establishment for introduced species", example = "established")
  private String degreeOfEstablishment;

  @Schema(description = "The pathway by which the species was introduced", example = "escape from cultivation")
  private String pathway;

  @Schema(description = "The threat or conservation status in this location", example = "LC")
  private String threatStatus;

  @Schema(description = "The date or date range of the distribution record", example = "2020-06-15")
  private String eventDate;

  @Schema(description = "Source of the distribution information", example = "National Flora Database")
  private String source;

  @Schema(description = "Additional remarks or notes about the distribution",
    example = "Common in mountain forests above 1000m elevation")
  private String remarks;
}
