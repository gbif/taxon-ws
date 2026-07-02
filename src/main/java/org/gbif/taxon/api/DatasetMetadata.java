package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.DatasetType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Basic metadata about a checklist dataset")
public class DatasetMetadata {

  @Schema(description = "The GBIF UUID of the dataset", example = "2d59e5db-57ad-41ff-97d6-11f5fb264527")
  private UUID key;

  @Schema(description = "The ChecklistBank numeric dataset key", example = "3")
  private Integer clbKey;

  @Schema(description = "The full title of the dataset", example = "Catalogue of Life")
  private String title;

  @Schema(description = "A short abbreviated name or acronym for the dataset", example = "COL")
  private String alias;

  @Schema(description = "The Digital Object Identifier of the dataset", example = "10.48580/dfpz")
  private String doi;

  @Schema(description = "The Digital Object Identifier of this specific version of the dataset", example = "10.48580/dfqs")
  private String versionDoi;

  @Schema(description = "Alternative identifiers for the dataset in other registries")
  private List<DatasetIdentifier> identifier;

  @Schema(description = "The version of the dataset", example = "2024-09-25")
  private String version;

  @Schema(description = "The type of dataset (e.g. TAXONOMIC, NOMENCLATURAL, ARTICLE)", example = "TAXONOMIC")
  private DatasetType type;

  @Schema(description = "Timestamp when the dataset was first created in ChecklistBank", example = "2024-03-15T10:00:00")
  private LocalDateTime created;

  @Schema(description = "Timestamp when the dataset metadata was last modified in ChecklistBank", example = "2024-03-15T10:30:00")
  private LocalDateTime modified;

  @Schema(description = "The publication date of the dataset as issued by the publisher", example = "2024-09-25")
  private String issued;

  @Schema(description = "Timestamp of the latest successful data import into ChecklistBank", example = "2024-03-16T02:00:00")
  private LocalDateTime imported;
}
