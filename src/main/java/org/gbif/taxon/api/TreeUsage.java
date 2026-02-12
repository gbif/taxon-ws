package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Extended simple usage for tree navigation with species counts")
public class TreeUsage extends SimpleUsage {

  @Schema(description = "Number of direct children which are accepted taxa of any rank but not synonyms", example = "7")
  private int children;

  @Schema(description = "Number of included species in this taxon", example = "127")
  private int species;
}
