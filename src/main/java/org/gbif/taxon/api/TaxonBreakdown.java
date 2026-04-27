package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.gbif.nameparser.api.Rank;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Hierarchical breakdown of accepted taxa with species counts.
 */
@Data
@Schema(description = "Hierarchical breakdown of accepted taxa with species counts at each level")
public class TaxonBreakdown {

  @Schema(requiredMode = REQUIRED, description = "The unique identifier for this taxon (dwc:taxonID)", example = "2435099")
  private String taxonID;

  @Schema(requiredMode = REQUIRED, description = "The taxonomic rank of the taxon (dwc:taxonRank)", example = "FAMILY")
  private Rank taxonRank;

  @Schema(requiredMode = REQUIRED, description = "The scientific name of the taxon without authorship (dwc:scientificName)", example = "Pinaceae")
  private String scientificName;

  @Schema(description = "The number of accepted species within this taxon", example = "250")
  private int species;

  @Schema(description = "Nested breakdown by all descendant taxa sharing the highest major Linnean rank")
  private List<TaxonBreakdown> breakdown;

}
