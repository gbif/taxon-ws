package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;


import org.gbif.nameparser.api.NameType;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Extended name usage class with additional taxonomic and nomenclatural details.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Extended taxon name usage with detailed taxonomic and nomenclatural information")
public class NameUsage extends NameUsageSimple {

  @Schema(description = "The identifier for the dataset", example = "7ddf754f-d193-4cc9-b351-99906754a03b")
  private UUID datasetKey;

  @Schema(description = "The identifier for the scientific name", example = "50123456")
  private String scientificNameID;

  @Schema(description = "The accepted name usage (for synonyms)")
  private String acceptedNameUsage;

  @Schema(description = "The parent name usage in the classification")
  private String parentNameUsage;

  @Schema(description = "The identifier of the original name usage (basionym)", example = "2435090")
  private String originalNameUsageID;

  @Schema(description = "The original name usage (basionym)")
  private String originalNameUsage;

  @Schema(description = "The taxon concept reference", example = "Smith 2020")
  private String nameAccordingTo;

  @Schema(description = "The publication where the name was first published", example = "Flora Europaea Vol. 1")
  private String namePublishedIn;

  @Schema(description = "An optional phrase appended to the name", example = "sensu lato")
  private String namePhrase;

  @Schema(description = "The nomenclatural status of the name", example = "valid")
  private String nomenclaturalStatus;

  @Schema(description = "The type of name (e.g., scientific, informal)", example = "SCIENTIFIC")
  private NameType nameType;

  @Schema(description = "Broad classification into taxonomic groups", example = "Plants")
  private TaxGroup taxonomicGroup;

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

  @Schema(description = "The unique identifier of the source dataset", example = "7ddf754f-d193-4cc9-b351-99906754a03b")
  private UUID sourceDatasetKey;

  @Schema(description = "The identifier in the source dataset", example = "taxon:12345")
  private String sourceID;

  @Schema(description = "Link to references or source information", example = "https://www.catalogueoflife.org/data/taxon/...")
  private URI references;

  @Schema(description = "List of data quality issues using ChecklistBank issue vocabulary")
  private List<String> issues;

  @Schema(description = "Remarks or notes about the taxon", example = "Common in mountainous regions of Europe")
  private String taxonRemarks;
}
