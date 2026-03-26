package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Various information about a taxon that is not coming from the dataset directly,
 * but is aggregated from other related sources such as the IUCN Red List, CITES, and the GRIIS lists.
 */
@Data
@Schema(description = "Aggregated conservation and invasive species information for a taxon " +
  "sourced from IUCN Red List, CITES appendices, and the Global Register of Introduced and Invasive Species (GRIIS)")
public class RelatedInfo {

  @Schema(description = "The taxon's entry in the IUCN Red List dataset, carrying the global threat status " +
    "in its taxonomicStatus field (e.g. LC, NT, VU, EN, CR, EW, EX)")
  private NameUsageSimple redlist;

  @Schema(description = "The taxon's entries in CITES appendices (I, II, or III). " +
    "Each entry represents the taxon's listing under a specific CITES appendix.")
  private List<NameUsageSimple> cites;

  @Schema(description = "Distribution records from the Global Register of Introduced and Invasive Species (GRIIS) " +
    "for countries where this taxon is considered invasive")
  private List<Distribution> griis;
}
