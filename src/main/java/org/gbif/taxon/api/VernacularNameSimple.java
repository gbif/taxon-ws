package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * A common or vernacular name for a taxon with just a name and language.
 */
@Data
@Schema(description = "A common or vernacular name for a taxon")
public class VernacularNameSimple {

  @Schema(requiredMode = REQUIRED, description = "The vernacular (common) name", example = "Silver Fir")
  private String vernacularName;

  @Schema(description = "The language of the vernacular name (ISO 639-2 code)", example = "eng")
  private String language;
}
