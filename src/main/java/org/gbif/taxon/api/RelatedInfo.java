package org.gbif.taxon.api;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Various information about a taxon that is not coming from the dataset directly,
 * but is aggregated from other related sources such as the IUCN Red List.
 */
@Data
@Schema(description = "Related taxon information")
public class RelatedInfo {

  @Schema(description = "The global threat status from the IUCN redlist dataset")
  private NameUsageSimple redlist;

  @Schema(description = "The CITES appendices the taxon appears in")
  private List<NameUsageSimple> cites;


  @Schema(description = "The GRIIS invasive species distributions for the related species")
  private List<Distribution> griis;
}
