package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * A common or vernacular name for a taxon.
 * Based on the GBIF Vernacular Name extension: https://rs.gbif.org/extension/gbif/1.0/vernacularname.xml
 */
@Data
@Schema(description = "A common or vernacular name for a taxon, based on the GBIF Vernacular Name extension")
public class VernacularName extends VernacularNameSimple {

  @Schema(description = "Temporal context specifying when the vernacular name was or is in use. " +
    "May be a year range or period description.",
    example = "1900-2000")
  private String temporal;

  @Schema(description = "The specific description of the place where this vernacular name is used (dwc:locality)",
    example = "Bavaria, southern Germany")
  private String locality;

  @Schema(description = "ISO 3166-1 alpha-2 or alpha-3 country code where this name is used (dwc:countryCode)",
    example = "DE")
  private String countryCode;

  @Schema(description = "The sex of the biological individual(s) to which this vernacular name applies (dwc:sex). " +
    "Useful for species where different sexes are known by different common names.",
    example = "male")
  private String sex;

  @Schema(description = "Indicates if this vernacular name is the preferred or most widely used common name for this taxon",
    example = "true")
  private Boolean preferredName;

  @Schema(description = "Bibliographic citation referencing the source of this vernacular name",
    example = "Flora of Germany, 3rd edition")
  private String source;

  @Schema(description = "Additional context or notes qualifying the usage of this vernacular name (dwc:taxonRemarks)",
    example = "Commonly used in forestry contexts")
  private String remarks;
}
