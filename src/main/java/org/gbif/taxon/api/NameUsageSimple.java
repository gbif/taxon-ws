package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxonomicStatus;
import lombok.Data;

import java.net.URI;
import java.util.UUID;

/**
 * Simplified taxon usage class containing core taxonomic information.
 */
@Data
@Schema(description = "Simplified taxon usage containing core taxonomic information")
public class NameUsageSimple extends ClassificationUsage {

  @Schema(description = "The identifier for the dataset", example = "7ddf754f-d193-4cc9-b351-99906754a03b")
  private UUID datasetKey;

  @Schema(description = "The identifier of the name", example = "1052714-2")
  private String scientificNameID;

  @Schema(description = "The identifier of the accepted taxon (for synonyms)", example = "2435098")
  private String acceptedNameUsageID;

  @Schema(description = "The identifier of the parent taxon in the classification", example = "2435001")
  private String parentNameUsageID;

  @Schema(description = "The taxonomic status of the taxon (e.g., accepted, synonym)", example = "accepted")
  private TaxonomicStatus taxonomicStatus;

  @Schema(description = "The nomenclatural code governing the taxon name", example = "BOTANICAL")
  private String nomenclaturalCode;

  @Schema(description = "Indicates whether the taxon is extinct", example = "false")
  private Boolean extinct;

  @Schema(description = "A link to a webpage for this name on the original, external site (dc:references)", example = "https://www.speciesplus.net/#/taxon_concepts/10836")
  private URI references;

  // the following are only present in specific lists when used in RelatedInfo
  @Schema(description = "The IUCN Red List threat status for this taxon. " +
    "Only populated when this usage appears in a RelatedInfo.redlist context. " +
    "Values follow IUCN Red List categories: EX (Extinct), EW (Extinct in the Wild), " +
    "CR (Critically Endangered), EN (Endangered), VU (Vulnerable), NT (Near Threatened), " +
    "LC (Least Concern), DD (Data Deficient), NE (Not Evaluated).",
    example = "LC")
  private String threatStatus;

  @Schema(description = "The CITES appendix under which this taxon is listed. " +
    "Only populated when this usage appears in a RelatedInfo.cites context. " +
    "Appendix I: species threatened with extinction; " +
    "Appendix II: species not necessarily threatened but requiring trade controls; " +
    "Appendix III: species protected in at least one country.",
    example = "II")
  private String citesAppendix;
}
