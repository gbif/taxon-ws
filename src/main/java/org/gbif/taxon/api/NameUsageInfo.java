package org.gbif.taxon.api;

import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;
import lombok.Data;

/**
 * Comprehensive taxon usage information including all related data.
 */
@Data
@Schema(description = "Comprehensive taxon usage information including all related data such as vernacular names, synonyms, media, distributions, and bibliography")
public class NameUsageInfo {

  @Schema(description = "The accepted taxon this info object is about")
  private NameUsage taxon;

  @Schema(description = "The major taxonomic group the taxon is considered in")
  private TaxGroup group;

  @Schema(description = "List of synonyms and nomenclatural combinations for the taxon")
  private Synonymy synonyms;

  @Schema(description = "List of parent taxa starting with the xxx")
  private List<NameUsageSimple> classification;

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
}
