package org.gbif.species.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * A common or vernacular name for a taxon.
 */
@Data
@Schema(description = "A common or vernacular name for a taxon")
public class VernacularName {

  @Schema(description = "The vernacular (common) name", example = "Silver Fir")
  private String vernacularName;

  @Schema(description = "The language of the vernacular name (ISO 639 code)", example = "en")
  private String language;

  @Schema(description = "Temporal context for the name usage", example = "1900-2000")
  private String temporal;

  @Schema(description = "Geographic locality where the name is used", example = "Central Europe")
  private String locality;

  @Schema(description = "ISO 3166-1 alpha-2 country code", example = "DE")
  private String countryCode;

  @Schema(description = "Sex to which the name applies (if applicable)", example = "male")
  private String sex;

  @Schema(description = "Indicates if this is the preferred vernacular name", example = "true")
  private Boolean preferredName;

  @Schema(description = "Source of the vernacular name information", example = "Flora of Germany")
  private String source;

  @Schema(description = "Additional remarks or notes about the vernacular name",
    example = "Commonly used in forestry contexts")
  private String remarks;
}
