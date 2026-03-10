package org.gbif.taxon.api;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxonomicStatus;
import lombok.Data;


import org.gbif.nameparser.api.Rank;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simplified taxon usage class containing core taxonomic information.
 */
@Data
@Schema(description = "Simplified taxon usage containing core taxonomic information")
public class NameUsageSimple {

  @Schema(description = "The identifier for the dataset", example = "7ddf754f-d193-4cc9-b351-99906754a03b")
  private UUID datasetKey;

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

  @Schema(description = "A link to a webpage for this name on the original, external site", example = "https://www.speciesplus.net/#/taxon_concepts/10836")
  private String link;

  @JsonAnyGetter
  @Schema(description = "Extension data to be rendered as additional properties")
  private final Map<String, Object> data = new HashMap<>();

  public void addData(String key, Object value) {
    data.put(key, value);
  }

  @Schema(description = "HTML formatted name with authorship and extinct dagger if applicable",
    example = "<i>Abies alba</i> Mill.")
  private String label;
}
