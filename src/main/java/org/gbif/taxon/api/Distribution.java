package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

/**
 * Geographic distribution information for a taxon.
 * Based on the GBIF Distribution extension: https://rs.gbif.org/extension/gbif/1.0/distribution.xml
 */
@Data
@Schema(description = "Geographic distribution information for a taxon, based on the GBIF Distribution extension")
public class Distribution {

  @Schema(description = "The identifier for the dataset", example = "7ddf754f-d193-4cc9-b351-99906754a03b")
  private UUID datasetKey;

  @Schema(description = "The unique identifier for this taxon (dwc:taxonID)", example = "2435099")
  private String taxonID;

  @Schema(description = "A CURIE identifier (scope:id) for the named area (dwc:locationID), " +
    "where the scope indicates the gazetteer. " +
    "Supported gazetteers: ISO (2-letter country codes), TDWG (World Geographical Scheme for Recording Plant Distributions), " +
    "TEOW (Terrestrial Ecoregions of the World), LONGHURST (Longhurst biogeographical provinces), " +
    "FAO (FAO fishing areas), WGSRPD (TDWG regions). " +
    "See https://github.com/CatalogueOfLife/coldp/blob/master/README.md#areaid",
    example = "TDWG:GER")
  private String locationID;

  @Schema(description = "The specific description of the place (dwc:locality). " +
    "The verbatim name of the area as used in the source.",
    example = "Central European Mountains")
  private String locality;

  @Schema(description = "ISO 3166-1 alpha-2 country code (dwc:countryCode)", example = "DE")
  private String countryCode;

  @Schema(description = "The age class or life stage of the organisms to which this distribution applies (dwc:lifeStage)",
    example = "adult")
  private String lifeStage;

  @Schema(description = "The process by which the organism became established at the location (dwc:establishmentMeans). " +
    "Recommended values: native, introduced, naturalised, invasive, managed, uncertain.",
    example = "native")
  private String establishmentMeans;

  @Schema(description = "The degree to which an organism survives, reproduces, and expands its range at the given place (dwc:degreeOfEstablishment). " +
    "Recommended values from the IUCN Invasive Species framework.",
    example = "established")
  private String degreeOfEstablishment;

  @Schema(description = "The process by which an organism came to be in a given place at a given time (dwc:pathway). " +
    "Recommended best practice is to use a controlled vocabulary such as the CBD pathway vocabulary.",
    example = "escape_from_cultivation")
  private String pathway;

  @Schema(description = "Flag to indicate that the introduced taxon is considered to be invasive in the given area",
    example = "true")
  private Boolean isInvasive;

  @Schema(description = "The IUCN Red List or equivalent threat/conservation status for the taxon in this location. " +
    "Recommended values: EX, EW, CR, EN, VU, NT, LC, DD, NE.",
    example = "LC")
  private String threatStatus;

  @Schema(description = "The date or date range during which this distribution record is valid (dwc:eventDate). " +
    "Recommended best practice is to use an encoding scheme such as ISO 8601.",
    example = "2020-06-15")
  private String eventDate;

  @Schema(description = "Identifier for the bibliographic reference which is the source of this distribution record",
    example = "R5678")
  private String referenceID;

  @Schema(description = "Additional remarks or notes about this distribution record (dwc:occurrenceRemarks)",
    example = "Common in mountain forests above 1000m elevation")
  private String remarks;
}
