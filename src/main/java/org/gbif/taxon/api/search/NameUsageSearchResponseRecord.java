package org.gbif.taxon.api.search;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;


import org.gbif.taxon.api.NameUsageSimple;
import org.gbif.taxon.api.VernacularNameSimple;


import java.util.List;

import lombok.Data;

@Data
public class NameUsageSearchResponseRecord {
  private NameUsageSimple usage;
  @Schema(description = "The major taxonomic group the taxon is considered in")
  private TaxGroup group;
  private List<NameUsageSimple> classification;
  private List<VernacularNameSimple> vernacularNames;
}
