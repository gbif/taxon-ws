package org.gbif.taxon.api;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Container for synonyms of a taxon organized by nomenclatural relationship type.
 */
@Data
@Schema(description = "Synonyms of a taxon organized by nomenclatural relationship type")
public class Synonymy {

  @Schema(description = "Homotypic (nomenclatural) synonyms that share the same type specimen as the accepted name. " +
    "These names are synonyms purely due to nomenclatural rules.")
  private List<NameUsageSimple> homotypic;

  @Schema(description = "Heterotypic (taxonomic) synonyms based on different type specimens, grouped by their common basionym. " +
    "Each inner list represents a homotypic group of names considered synonymous for taxonomic reasons.")
  private List<List<NameUsageSimple>> heterotypic;

  @Schema(description = "Names that have been misapplied to this taxon in literature, " +
    "i.e. used for a different taxon than the one they were originally described for.")
  private List<NameUsageSimple> misapplied;

  public int size() {
    return this.homotypic.size() + this.heterotypic.size() + this.misapplied.size();
  }

}
