package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxonomicStatus;
import lombok.Data;
import org.gbif.nameparser.api.Rank;

import java.net.URI;
import java.util.UUID;

/**
 * Extremely simple taxon usage containing only the name and taxonID.
 * Suitable for long classification lists.
 */
@Data
@Schema(description = "Extremely simple taxon usage containing only the name and taxonID. Suitable for long classification lists.")
public class ClassificationUsage {

  @Schema(description = "The unique identifier for this taxon", example = "2435099")
  private String taxonID;

  @Schema(description = "The scientific name without authorship", example = "Abies alba")
  private String scientificName;

  @Schema(description = "The authorship information for the scientific name", example = "Mill.")
  private String scientificNameAuthorship;

  @Schema(description = "The taxonomic rank of the taxon", example = "SPECIES")
  private Rank taxonRank;

  @Schema(description = "HTML formatted name with authorship and extinct dagger if applicable",
    example = "<i>Abies alba</i> Mill.")
  private String label;
}
