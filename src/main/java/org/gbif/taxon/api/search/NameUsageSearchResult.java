package org.gbif.taxon.api.search;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;
import lombok.Data;
import org.gbif.taxon.api.ClassificationUsage;
import org.gbif.taxon.api.NameUsage;
import org.gbif.taxon.api.VernacularNameSimple;

import java.util.List;

@Data
@Schema(description = "A single result from a name usage full-text search, including the matched taxon, it's classification, and matching vernacular names")
public class NameUsageSearchResult extends NameUsage {

  @Schema(description = "The major taxonomic group the taxon is considered in", example = "Gymnosperms")
  private TaxGroup taxonomicGroup;

  @Schema(description = "Ordered list of parent taxa from root to the direct parent of the matched taxon")
  private List<ClassificationUsage> classification;

  @Schema(description = "Vernacular names that matched the search query")
  private List<VernacularNameSimple> vernacularNames;
}
