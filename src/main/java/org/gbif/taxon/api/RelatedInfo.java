package org.gbif.taxon.api;

import java.net.URI;
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
  private String threatStatus;

  @Schema(description = "The global threat status from the IUCN redlist dataset")
  private NameUsageSimple threatStatusUsage;

  @Schema(description = "The CITES appendices the species appears in")
  private List<String> citesAppendix;

  @JsonIgnore
  public boolean hasContent() {
    return threatStatus != null || citesAppendix != null;
  }
}
