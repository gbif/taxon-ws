package org.gbif.species.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * Extended name usage class with additional taxonomic and nomenclatural details.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Extended taxon name usage with detailed taxonomic and nomenclatural information")
public class NameUsage extends SimpleUsage {

  @Schema(description = "The identifier for the scientific name", example = "50123456")
  private String scientificNameID;

  @Schema(description = "The accepted name usage (for synonyms)")
  private SimpleUsage acceptedNameUsage;

  @Schema(description = "The parent name usage in the classification")
  private SimpleUsage parentNameUsage;

  @Schema(description = "The identifier of the original name usage (basionym)", example = "2435090")
  private String originalNameUsageID;

  @Schema(description = "The original name usage (basionym)")
  private SimpleUsage originalNameUsage;

  @Schema(description = "The identifier of the reference where the name was published according to", example = "ref123")
  private String nameAccordingToID;

  @Schema(description = "The reference where the name was published according to", example = "Smith et al. 2020")
  private String nameAccordingTo;

  @Schema(description = "The identifier of the publication where the name was first published", example = "pub456")
  private String namePublishedInID;

  @Schema(description = "The publication where the name was first published", example = "Flora Europaea Vol. 1")
  private String namePublishedIn;

  @Schema(description = "An optional phrase appended to the name", example = "sensu lato")
  private String namePhrase;

  @Schema(description = "The nomenclatural status of the name", example = "valid")
  private String nomenclaturalStatus;

  @Schema(description = "The type of name (e.g., scientific, informal)", example = "SCIENTIFIC")
  private String nameType;

  @Schema(description = "Broad classification into taxonomic groups", example = "Plants")
  private String taxonomicGroup;

  @Schema(description = "The genus component of the scientific name", example = "Abies")
  private String genus;

  @Schema(description = "The infrageneric epithet (subgenus, section, etc.)", example = "Pseudotsuga")
  private String infragenericEpithet;

  @Schema(description = "The specific epithet (species name)", example = "alba")
  private String specificEpithet;

  @Schema(description = "The infraspecific epithet (subspecies, variety, form)", example = "alpina")
  private String infraspecificEpithet;

  @Schema(description = "The cultivar epithet for cultivated varieties", example = "Golden Sprite")
  private String cultivarEpithet;

  @Schema(description = "The unique identifier of the source dataset", example = "7ddf754f-d193-4cc9-b351-99906754a03b")
  private String sourceDatasetKey;

  @Schema(description = "The identifier in the source dataset", example = "taxon:12345")
  private String sourceID;

  @Schema(description = "Link to references or source information", example = "https://www.catalogueoflife.org/data/taxon/...")
  private String references;

  @Schema(description = "List of data quality issues using ChecklistBank issue vocabulary")
  private List<String> issues;

  @Schema(description = "Remarks or notes about the taxon", example = "Common in mountainous regions of Europe")
  private String taxonRemarks;
}
