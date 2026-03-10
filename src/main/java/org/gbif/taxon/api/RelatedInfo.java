package org.gbif.taxon.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;
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
  private final List<NameUsageSimple> cites = new ArrayList<>();


  @Schema(description = "The GRIIS invasive species lists the taxon appears in with a country given")
  private final List<NameUsageSimple> griis = new ArrayList<>();
}
