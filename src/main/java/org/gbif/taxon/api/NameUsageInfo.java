package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;
import lombok.Data;
import org.gbif.nameparser.api.NameType;

import java.net.URI;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Comprehensive taxon usage information including all data we know about the taxon, such as vernacular names, synonyms, media, distributions, and bibliography.
 */
@Data
@Schema(description = "Comprehensive taxon information including all related data such as vernacular names, synonyms, media, distributions, and bibliography")
public class NameUsageInfo extends NameUsage {

  @Schema(description = "The accepted name usage (for synonyms)", example = "Abies alba Mill.")
  private String acceptedNameUsage;

  @Schema(description = "The identifier of the original name usage (basionym)", example = "2435090")
  private String originalNameUsageID;

  @Schema(description = "The original name usage (basionym)", example = "Pinus alba Mill.")
  private String originalNameUsage;

  @Schema(description = "The taxon concept reference", example = "Smith 2020")
  private String nameAccordingTo;

  @Schema(description = "The bibliographic reference identifier for the publication where the name was first published", example = "Greuter1998")
  private String namePublishedInID;

  @Schema(requiredMode = REQUIRED, description = "The type of name (e.g., scientific, informal)", example = "SCIENTIFIC")
  private NameType nameType;

  @Schema(description = "An optional phrase appended to the name", example = "sensu lato")
  private String namePhrase;

  @Schema(description = "The nomenclatural status of the name", example = "valid")
  private String nomenclaturalStatus;

  @Schema(description = "The genus component of the scientific name", example = "Abies")
  private String genericName;

  @Schema(description = "The infrageneric epithet (subgenus, section, etc.)", example = "Pseudotsuga")
  private String infragenericEpithet;

  @Schema(description = "The specific epithet (species name)", example = "alba")
  private String specificEpithet;

  @Schema(description = "The infraspecific epithet (subspecies, variety, form)", example = "alpina")
  private String infraspecificEpithet;

  @Schema(description = "The cultivar epithet for cultivated varieties", example = "Golden Sprite")
  private String cultivarEpithet;

  @Schema(description = "Environments where the taxon occurs (marine, freshwater, terrestrial, brackish)",
      example = "[\"terrestrial\", \"freshwater\"]")
  private List<String> environment;

  @Schema(description = "The major taxonomic group the taxon is considered in", example = "Gymnosperms")
  private TaxGroup taxonomicGroup;

  @Schema(description = "List of data quality issues using ChecklistBank issue vocabulary",
      example = "[\"PUBLISHED_BEFORE_1753\", \"BASIONYM_ID_INVALID\"]")
  private List<String> issues;

  @Schema(description = "Remarks or notes about the taxon", example = "Common in mountainous regions of Europe")
  private String taxonRemarks;

  @Schema(requiredMode = REQUIRED, description = "Link to the taxon page on ChecklistBank",
      example = "https://www.checklistbank.org/dataset/3/taxon/2435099")
  private URI checklistbankURL;


  @Schema(description = "Ordered list of parent taxa from the root of the classification down to the direct parent of this taxon")
  private List<ClassificationUsage> classification;

  @Schema(description = "List of synonyms and nomenclatural combinations for the taxon")
  private Synonymy synonyms;

  @Schema(description = "List of vernacular (common) names for the taxon")
  private List<VernacularName> vernacularNames;

  @Schema(description = "Media items (images, videos, sounds) associated with the taxon")
  private List<Media> media;

  @Schema(description = "Distribution information for the taxon")
  private List<Distribution> distributions;

  @Schema(description = "Bibliographic references related to the taxon, keyed by their identifier. A lookup for referenceIDs found in taxon, distributions or vernacular names.")
  private List<Reference> bibliography;

  @Schema(description = "Measurements or facts about the taxon")
  private List<MeasurementOrFact> measurementOrFacts;

  @Schema(description = "Alternative identifiers for the taxon, each given as a CURIE-style scope, its local id and a resolvable link")
  private List<Identifier> identifiers;
}
