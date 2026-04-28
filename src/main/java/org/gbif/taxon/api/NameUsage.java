package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.gbif.nameparser.api.NameType;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Extended name usage class with additional taxonomic and nomenclatural details.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Extended taxon name usage with detailed taxonomic and nomenclatural information")
public class NameUsage extends NameUsageSimple {

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

  @Schema(description = "An optional phrase appended to the name", example = "sensu lato")
  private String namePhrase;

  @Schema(description = "The nomenclatural status of the name", example = "valid")
  private String nomenclaturalStatus;

  @Schema(requiredMode = REQUIRED, description = "The type of name (e.g., scientific, informal)", example = "SCIENTIFIC")
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

  @Schema(description = "List of data quality issues using ChecklistBank issue vocabulary",
    example = "[\"PUBLISHED_BEFORE_1753\", \"BASIONYM_ID_INVALID\"]")
  private List<String> issues;

  @Schema(description = "Remarks or notes about the taxon", example = "Common in mountainous regions of Europe")
  private String taxonRemarks;
}
