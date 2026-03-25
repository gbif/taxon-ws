package org.gbif.taxon.api.search;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;


import org.gbif.taxon.api.NameUsageSimple;
import org.gbif.taxon.api.VernacularNameSimple;


import java.util.List;

import lombok.Data;

@Data
@Schema(description = "A single result from a name usage full-text search, including the matched taxon, its classification, and matching vernacular names")
public class NameUsageSearchResult {

  @Schema(description = "The matched taxon name usage")
  private NameUsageSimple taxon;

  @Schema(description = "The major taxonomic group the taxon is considered in", example = "Gymnosperms")
  private TaxGroup group;

  @Schema(description = "Ordered list of parent taxa from root to the direct parent of the matched taxon")
  private List<NameUsageSimple> classification;

  @Schema(description = "Vernacular names that matched the search query")
  private List<VernacularNameSimple> vernacularNames;

  @Schema(description = "Relevance score of this result", example = "1.23")
  private Double score;
}
