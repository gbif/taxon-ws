package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.net.URI;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * A taxonomic checklist served by this API, linking the GBIF dataset UUID to the ChecklistBank
 * dataset that currently backs it.
 * For the Catalogue of Life the ChecklistBank key changes with every new edition, so this is the
 * authoritative answer to which version GBIF is using right now.
 */
@Data
@Schema(description = "A taxonomic checklist served by this API, with the ChecklistBank dataset currently backing it")
public class Checklist {

  @Schema(requiredMode = REQUIRED, description = "The GBIF dataset key (dwc:datasetID)",
    example = "7ddf754f-d193-4cc9-b351-99906754a03b")
  private UUID key;

  @Schema(requiredMode = REQUIRED, description = "The ChecklistBank dataset key that currently backs this checklist. " +
    "For the Catalogue of Life this changes with every new edition",
    example = "315557")
  private int clbDatasetKey;

  @Schema(description = "Short unique name of the exact version in use, if given (dcterms:alternative)",
    example = "COL26.6 XR")
  private String alias;

  @Schema(requiredMode = REQUIRED, description = "The title of the checklist (dcterms:title)", example = "Catalogue of Life")
  private String title;

  @Schema(description = "The version of the checklist in use (dcterms:hasVersion)", example = "June 2026")
  private String version;

  @Schema(description = "The date the version in use was published, which can be a year or year-month only (dcterms:issued)",
    example = "2026-06-15")
  private String issued;

  @Schema(description = "The DOI of the checklist across all its versions (dcterms:identifier)", example = "10.48580/dgy8b")
  private String doi;

  @Schema(description = "The DOI of the exact version in use", example = "10.48580/dgxsq")
  private String versionDoi;

  @Schema(description = "The homepage of the checklist (dcterms:source)", example = "https://www.catalogueoflife.org")
  private URI url;

  @Schema(description = "A logo for the checklist (foaf:logo)", example = "https://api.checklistbank.org/dataset/315557/logo?size=MEDIUM")
  private URI logo;

  @Schema(description = "The total number of name usages in the version in use", example = "7991756")
  private Integer nameUsageCount;

  @Schema(description = "A ColDP archive download of the exact version in use",
    example = "https://api.checklistbank.org/dataset/315557/export.zip?format=ColDP&extended=true")
  private URI downloadUrlColdp;

  @Schema(description = "A Darwin Core archive download of the exact version in use",
    example = "https://api.checklistbank.org/dataset/315557/export.zip?format=DwCA&extended=true")
  private URI downloadUrlDwca;
}
