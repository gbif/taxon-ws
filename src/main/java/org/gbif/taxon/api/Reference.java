package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * A bibliographic reference or citation for a taxon.
 * Based on the GBIF References extension: https://rs.gbif.org/extension/gbif/1.0/references.xml
 */
@Data
@Schema(description = "A bibliographic reference or citation for a taxon")
public class Reference {

  @Schema(description = "An identifier for the reference in the dataset",
    example = "Smith2020")
  private String referenceID;

  @Schema(description = "The Digital Object Identifier (DOI) for the reference",
    example = "10.1234/example.doi")
  private String doi;

  @Schema(description = "The full, unparsed bibliographic citation for the reference (dc:bibliographicCitation). " +
    "Should include author(s), year, title, journal/book, volume, pages.",
    example = "Smith, J. & Jones, A. (2020). New Species of Abies. Journal of Botany, 45(2), 123-145.")
  private String citation;

  @Schema(description = "A link to the publication",
      example = "https://www.biodiversitylibrary.org/page/52334711#page/531/mode/1up")
  private String url;

  @Schema(description = "Taxon-specific annotation or notes about how this reference relates to the taxon (dwc:taxonRemarks)",
    example = "Original description of the species")
  private String remarks;
}
