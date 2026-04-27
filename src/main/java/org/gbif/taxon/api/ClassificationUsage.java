package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.gbif.nameparser.api.Rank;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Extremely simple taxon usage containing only the name and taxonID.
 * Suitable for long classification lists.
 */
@Data
@Schema(description = "Extremely simple taxon usage containing only the name and taxonID. Suitable for long classification lists.")
public class ClassificationUsage {

  @Schema(requiredMode = REQUIRED, description = "The unique identifier for this taxon", example = "2435099")
  private String taxonID;

  @Schema(requiredMode = REQUIRED, description = "The scientific name without authorship", example = "Abies alba")
  private String scientificName;

  @Schema(description = "The authorship information for the scientific name", example = "Mill.")
  private String scientificNameAuthorship;

  @Schema(requiredMode = REQUIRED, description = "The taxonomic rank of the taxon", example = "SPECIES")
  private Rank taxonRank;

  @Schema(requiredMode = REQUIRED, description = "HTML formatted name with authorship and extinct dagger if applicable",
    example = "<i>Abies alba</i> Mill.")
  private String label;
}
