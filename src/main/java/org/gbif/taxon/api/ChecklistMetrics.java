package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.DataFormat;
import life.catalogue.api.vocab.DatasetOrigin;
import life.catalogue.api.vocab.ImportState;
import life.catalogue.api.vocab.NomStatus;
import life.catalogue.api.vocab.Origin;
import life.catalogue.api.vocab.TaxonomicStatus;
import lombok.Data;


import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.NomCode;
import org.gbif.nameparser.api.Rank;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Schema(description = "Metrics for a checklist dataset")
public class ChecklistMetrics {
  private DatasetOrigin origin;
  private DataFormat format;
  private LocalDateTime download;
  private int attempt;
  private ImportState state;
  private LocalDateTime started;
  private LocalDateTime finished;

  private Integer bareNameCount;
  private Integer distributionCount;
  private Integer estimateCount;
  private Integer mediaCount;
  private Integer nameCount;
  private Integer referenceCount;
  private Integer synonymCount;
  private Integer taxonCount;
  private Integer treatmentCount;
  private Integer typeMaterialCount;
  private Integer vernacularCount;

  private Map<NomCode, Integer> namesByCodeCount;
  private Map<Rank, Integer> namesByRankCount;
  private Map<NameType, Integer> namesByTypeCount;
  private Map<Rank, Integer> taxaByRankCount;
  private Map<TaxonomicStatus, Integer> usagesByStatusCount;
  private Map<Origin, Integer> usagesByOriginCount;
}
