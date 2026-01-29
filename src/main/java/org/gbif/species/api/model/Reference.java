package org.gbif.species.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * A bibliographic reference or citation.
 */
@Data
@Schema(description = "A bibliographic reference or citation")
public class Reference {

  @Schema(description = "The unique identifier for this reference", example = "ref123456")
  private String referenceID;

  @Schema(description = "The Digital Object Identifier (DOI) for the reference", example = "10.1234/example.doi")
  private String doi;

  @Schema(description = "The full citation text", 
          example = "Smith, J. & Jones, A. (2020). New Species of Abies. Journal of Botany, 45(2), 123-145.")
  private String citation;

  @Schema(description = "Additional remarks or notes about the reference", 
          example = "Original description of the species")
  private String remarks;
}
