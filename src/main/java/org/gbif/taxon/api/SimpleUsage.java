package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxonomicStatus;
import lombok.Data;


import org.gbif.nameparser.api.Rank;

/**
 * Simplified taxon usage class containing core taxonomic information.
 */
@Data
@Schema(description = "Simplified taxon usage containing core taxonomic information")
public class SimpleUsage {

  @Schema(description = "The unique identifier for this taxon", example = "2435099")
  private String taxonID;

  @Schema(description = "The identifier of the accepted taxon (for synonyms)", example = "2435098")
  private String acceptedNameUsageID;

  @Schema(description = "The identifier of the parent taxon in the classification", example = "2435001")
  private String parentNameUsageID;

  @Schema(description = "The scientific name without authorship", example = "Abies alba")
  private String scientificName;

  @Schema(description = "The authorship information for the scientific name", example = "Mill.")
  private String scientificNameAuthorship;

  @Schema(description = "The taxonomic rank of the taxon", example = "SPECIES")
  private Rank taxonRank;

  @Schema(description = "The taxonomic status of the taxon (e.g., accepted, synonym)", example = "accepted")
  private TaxonomicStatus taxonomicStatus;

  @Schema(description = "The nomenclatural code governing the taxon name", example = "ICNAFP")
  private String nomenclaturalCode;

  @Schema(description = "Indicates whether the taxon is extinct", example = "false")
  private Boolean extinct;

  @Schema(description = "HTML formatted name with authorship and extinct dagger if applicable",
    example = "<i>Abies alba</i> Mill.")
  private String label;
}
