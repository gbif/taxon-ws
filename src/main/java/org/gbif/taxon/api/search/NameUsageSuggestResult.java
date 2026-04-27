/*
 * Copyright 2020 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.taxon.api.search;

import io.swagger.v3.oas.annotations.media.Schema;
import life.catalogue.api.vocab.TaxGroup;
import life.catalogue.api.vocab.TaxonomicStatus;
import lombok.Data;
import org.gbif.nameparser.api.Rank;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Class used for returning results of a taxon suggest operation.
 * This class contains additional attributes that are required for displaying/providing textual information.
 */
@Data
@Schema(description = "A single result from a name usage prefix suggestion search")
public class NameUsageSuggestResult {

  @Schema(requiredMode = REQUIRED, description = "The unique identifier for this taxon within the dataset", example = "CXA")
  private String taxonID;

  @Schema(requiredMode = REQUIRED, description = "The identifier for the scientific name", example = "50123456")
  private String scientificNameID;

  // The name matching the search phrase: an accepted name/synonym/bare name
  @Schema(requiredMode = REQUIRED, description = "The scientific name matching the search prefix", example = "Abies alba")
  private String scientificName;

  @Schema(description = "The taxonomic rank of the suggested name", example = "SPECIES")
  private Rank taxonRank;

  @Schema(description = "The taxonomic status of the taxon (e.g., accepted, synonym)", example = "accepted")
  private TaxonomicStatus taxonomicStatus;

  @Schema(description = "The nomenclatural code governing the taxon name", example = "BOTANICAL")
  private String nomenclaturalCode;

  @Schema(description = "The identifier of the accepted taxon (for synonyms)", example = "2435098")
  private String acceptedNameUsageID;

  @Schema(description = "The accepted name (for synonyms)", example = "Abies alba Mill.")
  private String acceptedNameUsage;

  @Schema(description = "The major taxonomic group the taxon belongs to", example = "Gymnosperms")
  private TaxGroup group;

  // The classification context to report in the suggestion hint.
  // For species this is the first taxon above genus level, mostly the family.
  @Schema(requiredMode = REQUIRED, description = "Classification context shown as a hint, typically the family or order name", example = "Pinaceae")
  private String context;

}
