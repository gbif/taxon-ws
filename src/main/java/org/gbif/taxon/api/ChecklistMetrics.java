package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.*;
import lombok.Data;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.NomCode;
import org.gbif.nameparser.api.Rank;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "Metrics and import statistics for a checklist dataset")
public class ChecklistMetrics {

  @Schema(description = "The origin type of the dataset (e.g. EXTERNAL, MANAGED, RELEASED)", example = "EXTERNAL")
  private DatasetOrigin origin;

  @Schema(description = "The data format of the dataset source (e.g. DWCA, COLDP, ACEF)", example = "COLDP")
  private DataFormat format;

  @Schema(description = "Timestamp when the dataset was last downloaded or imported", example = "2024-03-15T10:30:00")
  private LocalDateTime download;

  @Schema(description = "Sequential import attempt number for this dataset", example = "5")
  private int attempt;

  @Schema(description = "The state of the latest import (e.g. FINISHED, FAILED, PROCESSING)", example = "FINISHED")
  private ImportState state;

  @Schema(description = "Timestamp when the latest import started", example = "2024-03-15T10:00:00")
  private LocalDateTime started;

  @Schema(description = "Timestamp when the latest import finished", example = "2024-03-15T10:30:00")
  private LocalDateTime finished;

  @Schema(description = "Number of bare names (name-only records without a usage context)", example = "120")
  private Integer bareNameCount;

  @Schema(description = "Number of distribution records in the dataset", example = "4500")
  private Integer distributionCount;

  @Schema(description = "Number of species estimate records", example = "30")
  private Integer estimateCount;

  @Schema(description = "Number of media records (images, sounds, videos) in the dataset", example = "8200")
  private Integer mediaCount;

  @Schema(description = "Total number of scientific names in the dataset", example = "55000")
  private Integer nameCount;

  @Schema(description = "Number of bibliographic reference records in the dataset", example = "12000")
  private Integer referenceCount;

  @Schema(description = "Number of synonymic name usages in the dataset", example = "18000")
  private Integer synonymCount;

  @Schema(description = "Total number of accepted taxon records in the dataset", example = "37000")
  private Integer taxonCount;

  @Schema(description = "Number of treatment records (descriptive text) in the dataset", example = "200")
  private Integer treatmentCount;

  @Schema(description = "Number of type material records in the dataset", example = "500")
  private Integer typeMaterialCount;

  @Schema(description = "Number of vernacular (common) name records in the dataset", example = "9000")
  private Integer vernacularNameCount;

  @Schema(description = "Count of names grouped by nomenclatural code (e.g. ZOOLOGICAL, BOTANICAL)",
    example = "{\"BOTANICAL\": 42000, \"ZOOLOGICAL\": 13000}")
  private Map<NomCode, Integer> namesByCodeCount;

  @Schema(description = "Count of names grouped by taxonomic rank (e.g. SPECIES, GENUS, FAMILY)",
    example = "{\"SPECIES\": 25000, \"GENUS\": 3000, \"FAMILY\": 400}")
  private Map<Rank, Integer> namesByRankCount;

  @Schema(description = "Count of names grouped by name type (e.g. SCIENTIFIC, HYBRID_FORMULA, INFORMAL)",
    example = "{\"SCIENTIFIC\": 53000, \"INFORMAL\": 1500, \"HYBRID_FORMULA\": 500}")
  private Map<NameType, Integer> namesByTypeCount;

  @Schema(description = "Count of accepted taxa grouped by taxonomic rank",
    example = "{\"SPECIES\": 18000, \"GENUS\": 2500, \"FAMILY\": 350}")
  private Map<Rank, Integer> taxaByRankCount;

  @Schema(description = "Count of name usages grouped by taxonomic status (e.g. ACCEPTED, SYNONYM, AMBIGUOUS_SYNONYM)",
    example = "{\"ACCEPTED\": 37000, \"SYNONYM\": 18000, \"AMBIGUOUS_SYNONYM\": 200}")
  private Map<TaxonomicStatus, Integer> usagesByStatusCount;

  @Schema(description = "Count of name usages grouped by origin (e.g. SOURCE, DENORMED_CLASSIFICATION, VERBATIM_ACCEPTED)",
    example = "{\"SOURCE\": 50000, \"DENORMED_CLASSIFICATION\": 4000, \"VERBATIM_ACCEPTED\": 1000}")
  private Map<Origin, Integer> usagesByOriginCount;
}
