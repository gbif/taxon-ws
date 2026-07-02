package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "An alternative identifier, given as a CURIE-style scope, its local id and a resolvable link")
public class Identifier {

  @Schema(description = "The CURIE-style scope (registry) the identifier belongs to, e.g. doi, gbif, col, lsid",
    example = "doi")
  private String scope;

  @Schema(description = "The local identifier within the scope", example = "10.5281/zenodo.6407053")
  private String id;

  @Schema(description = "A resolvable URL for the identifier, built from the scope's resolver template if known",
    example = "https://doi.org/10.5281/zenodo.6407053")
  private String url;
}
