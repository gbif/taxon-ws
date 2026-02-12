package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.net.URI;
import java.util.List;

/**
 * Comprehensive taxon usage information including all related data.
 */
@Data
@Schema(description = "Comprehensive taxon usage information including all related data such as vernacular names, synonyms, media, distributions, and bibliography")
public class UsageInfo {

  @Schema(description = "The accepted taxon this info object is about")
  private NameUsage taxon;

  @Schema(description = "List of synonyms and nomenclatural combinations for the taxon")
  private List<SimpleUsage> synonyms;

  @Schema(description = "List of parent taxa starting with the xxx")
  private List<SimpleUsage> classification;

  @Schema(description = "List of vernacular (common) names for the taxon")
  private List<VernacularName> vernacularNames;

  @Schema(description = "Media items (images, videos, sounds) associated with the taxon")
  private List<Media> media;

  @Schema(description = "Distribution information for the taxon")
  private List<Distribution> distributions;

  @Schema(description = "Bibliographic references related to the taxon, keyed by their identifier")
  private List<Reference> bibliography;

  @Schema(description = "Measurements or facts about the taxon")
  private List<MeasurementOrFact> measurementOrFacts;

  @Schema(description = "Link to the taxon page on ChecklistBank",
    example = "https://www.checklistbank.org/dataset/3/taxon/2435099")
  private URI checklistBankLink;

  @Schema(description = "Environments where the taxon occurs (marine, freshwater, terrestrial, brackish)")
  private List<String> environment;

  @Schema(description = "IUCN Red List conservation status", example = "LC")
  private String iucnRedlistStatus;

  @Schema(description = "CITES appendix designation (I, II, III)", example = "II")
  private String citesAppendix;

  @Schema(description = "Date when the taxon was added to CITES", example = "1975-07-01")
  private String citesDateAdded;
}
