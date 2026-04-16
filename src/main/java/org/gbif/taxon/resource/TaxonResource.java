/*
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
package org.gbif.taxon.resource;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import life.catalogue.api.search.NameUsageRequest;
import life.catalogue.api.vocab.*;
import org.gbif.api.documentation.CommonParameters;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.NomCode;
import org.gbif.nameparser.api.Rank;
import org.gbif.taxon.api.NameUsageInfo;
import org.gbif.taxon.api.NameUsageSimple;
import org.gbif.taxon.api.RelatedInfo;
import org.gbif.taxon.api.TaxonBreakdown;
import org.gbif.taxon.api.search.*;
import org.gbif.taxon.dao.TaxonDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.UUID;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Species API",
            version = "v2",
            description = """
                This API provides access to species (name usage) data indexed by ChecklistBank for GBIF.
                It exposes taxonomic name usages from registered checklist datasets including scientific names,
                synonymy, vernacular names, geographic distributions, media, bibliographic references, and
                measurements or facts. Taxon keys are scoped to individual datasets and are not global identifiers.
                """,
            termsOfService = "https://www.gbif.org/terms"),
    servers = {
      @Server(url = "https://api.gbif.org/v2/", description = "Production"),
      @Server(url = "https://api.gbif-test.org/v2/", description = "Test")
    })
@Tag(name = "Species", description = "Species indexed by ChecklistBank for GBIF")
@RequestMapping(value = "taxon", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class TaxonResource {
  private static final Logger LOG = LoggerFactory.getLogger(TaxonResource.class);

  private final TaxonDao dao;

  public TaxonResource(TaxonDao taxonDao) {
    this.dao = taxonDao;
  }

  @Operation(
    operationId = "getTaxon",
    summary = "Get a single taxon name usage",
    description = "Returns the simplified name usage for the given taxon key within a dataset, " +
      "including its scientific name, rank, taxonomic status, and classification identifiers."
  )
  @ApiResponse(responseCode = "200", description = "Taxon name usage")
  @ApiResponse(responseCode = "404", description = "Taxon not found")
  @GetMapping("/{datasetKey}/{taxonKey}")
  public NameUsageSimple get(
      @PathVariable("datasetKey")
      @Parameter(
          description = "UUID for the dataset key",
          example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
      )
      UUID datasetKey,
      @PathVariable("taxonKey")
      @Parameter(
        description = "Taxon key scoped within the dataset",
        example = "CXA"
      )
      String taxonKey
    ) {
    return dao.get(datasetKey, taxonKey);
  }

  @Operation(
    operationId = "getTaxonInfo",
    summary = "Get full taxon information",
    description = "Returns the comprehensive name usage information for the given taxon, aggregating all related data: " +
      "synonymy (homotypic, heterotypic, misapplied), classification, vernacular names, " +
      "geographic distributions, media, bibliographic references, and measurements or facts."
  )
  @ApiResponse(responseCode = "200", description = "Full taxon information")
  @ApiResponse(responseCode = "404", description = "Taxon not found")
  @GetMapping("/{datasetKey}/{taxonKey}/info")
  public NameUsageInfo getInfo(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
    )
    UUID datasetKey,
    @PathVariable("taxonKey")
    @Parameter(
      description = "Taxon key scoped within the dataset",
      example = "CXA"
    )
    String taxonKey
  ) {
    return dao.getInfo(datasetKey, taxonKey);
  }

  @Operation(
    operationId = "getTaxonBreakdown",
    summary = "Get descendant species count breakdown",
    description = "Returns a breakdown by all descendant taxa sharing the highest major Linnean rank, " +
      "counting the number of accepted species within each group."
  )
  @ApiResponse(responseCode = "200", description = "Taxonomic breakdown")
  @ApiResponse(responseCode = "404", description = "Taxon not found")
  @GetMapping("/{datasetKey}/{taxonKey}/breakdown")
  public TaxonBreakdown breakdown(
      @PathVariable("datasetKey")
      @Parameter(
        description = "UUID for the dataset key",
        example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
      )
      UUID datasetKey,
      @PathVariable("taxonKey")
      @Parameter(
        description = "Taxon key scoped within the dataset",
        example = "CXA"
      )
      String taxonKey
    ) {
    return dao.childrenBreakdown(datasetKey, taxonKey);
  }

  @Operation(
    operationId = "getRelatedUsages",
    summary = "Get name usages for this taxon from other datasets",
    description = "Returns name usages matching this taxon from other checklist datasets registered in ChecklistBank. " +
      "Results can be filtered by dataset type, specific dataset keys, or publisher keys."
  )
  @ApiResponse(responseCode = "200", description = "Related name usages")
  @ApiResponse(responseCode = "404", description = "Taxon not found")
  @GetMapping("/{datasetKey}/{taxonKey}/related")
  public List<NameUsageSimple> getRelated(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "2d59e5db-57ad-41ff-97d6-11f5fb264527",
      in = ParameterIn.PATH
    )
    UUID datasetKey,
    @PathVariable("taxonKey")
    @Parameter(
      description = "Taxon key scoped within the dataset",
      example = "CXA"
    )
    String taxonKey,
    @RequestParam(name = "datasetType", required = false)
    @Parameter(
      description = "Optional dataset type filter, repeatable: e.g. `?datasetType=article&datasetType=nomenclatural`",
      explode = Explode.TRUE,
      array = @ArraySchema(schema = @Schema(implementation = DatasetType.class))
    )
    List<DatasetType> datasetTypes,
    @RequestParam(name = "datasetKey", required = false)
    @Parameter(
      description = "Optional dataset key filter, repeatable: e.g. " +
        "`?datasetKey=7ddf754f-d193-4cc9-b351-99906754a03b&datasetKey=2d59e5db-57ad-41ff-97d6-11f5fb264527`",
      explode = Explode.TRUE,
      array = @ArraySchema(schema = @Schema(type = "uuid"))
    )
    List<UUID> datasetKeys,
    @RequestParam(name = "publisherKey", required = false)
    @Parameter(
      description = "Optional publisher key filter, repeatable: e.g. `?publisherKey=<uuid>&publisherKey=<uuid>`",
      explode = Explode.TRUE,
      array = @ArraySchema(schema = @Schema(type = "string", format = "uuid"))
    )
    List<UUID> publisherKeys
  ) {
    return dao.listRelated(datasetKey, taxonKey, datasetTypes, datasetKeys, publisherKeys);
  }


  @Hidden
  @GetMapping("/{datasetKey}/{taxonKey}/relatedInfo")
  public RelatedInfo getRelatedInfo(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
    )
    UUID datasetKey,
    @PathVariable("taxonKey")
    @Parameter(
      description = "Taxon key scoped within the dataset",
      example = "CXA"
    )
    String taxonKey
  ) {
    return dao.listRelatedInfo(datasetKey, taxonKey);
  }

  @Operation(
    operationId = "searchNames",
    summary = "Full text search over name usages",
    description = "Full-text search of name usages covering the scientific and vernacular names.\n\n" +
      "Results are ordered by relevance by default as this search usually returns a lot of results."
  )
  @Tag(name = "Searching names")
  @NameUsageSearchParameters
  @SearchParameters
  @SortParameters
  @CommonParameters.QParameter
  @Pageable.OffsetLimitParameters
  @FacetedSearchRequest.FacetParameters
  @ApiResponse(responseCode = "200", description = "Name usages found")
  @GetMapping("/search/{datasetKey}")
  public SearchResponse<NameUsageSearchResult, NameUsageSearchParameter> search(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
    )
    UUID datasetKey,
    @Parameter(hidden = true) NameUsageSearchRequest request
  ) {
    setDatasetKey(request, datasetKey);
    return dao.search(request);
  }

  private static void setDatasetKey(SearchRequest<NameUsageSearchParameter> request, UUID datasetKey) {
    var existing = request.getParameters().get(NameUsageSearchParameter.DATASET_KEY);
    if (existing != null) {
      existing.clear();
    }
    request.addParameter(NameUsageSearchParameter.DATASET_KEY, datasetKey.toString());
  }

  @Operation(
    operationId = "suggestNames",
    summary = "Suggestion service for name usages",
    description = "Prefix search of name usages covering the scientific and vernacular names.\n\n" +
      "Results are ordered by relevance by default as this search usually returns a lot of results."
  )
  @Tag(name = "Searching names")
  @NameUsageSearchParameters
  @SortParameters
  @CommonParameters.QParameter
  @Pageable.OffsetLimitParameters
  @FacetedSearchRequest.FacetParameters
  @ApiResponse(responseCode = "200", description = "Name usages found")
  @GetMapping("/suggest/{datasetKey}")
  public List<NameUsageSuggestResult> suggest(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
    )
    UUID datasetKey,
    @Parameter(hidden = true) NameUsageSuggestRequest request
  ) {
    setDatasetKey(request, datasetKey);
    return dao.suggest(request);
  }



  /* Same parameters for search and suggest queries. */
  @Target({METHOD, ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Parameters(
    value = {
      @Parameter(
        name = "taxonID",
        description = "Filters by any of the higher Linnean rank keys. Note this is within the respective checklist " +
          "and not searching NUB keys across all checklists.",
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "rank",
        description = "Filters by taxonomic rank.",
        schema = @Schema(implementation = Rank.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "status",
        description = "Filters by the taxonomic status as given in our https://api.gbif.org/v1/enumeration/basic/TaxonomicStatus[TaxonomicStatus enum].",
        schema = @Schema(implementation = TaxonomicStatus.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "extinct",
        description = "Filters by extinction status.",
        schema = @Schema(implementation = Boolean.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "environment",
        description = "Filters by environment values.",
        schema = @Schema(implementation = Environment.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "group",
        description = "Filters for name usages with a specific taxonomic group.",
        schema = @Schema(implementation = TaxGroup.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "code",
        description = "Filters by the nomenclatural code.",
        schema = @Schema(implementation = NomCode.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "nameType",
        description = "Filters by the name type as given in our https://api.gbif.org/v1/enumeration/basic/NameType[NameType enum].",
        schema = @Schema(implementation = NameType.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "author",
        description = "Filters for name usages with a specific author.",
        schema = @Schema(implementation = String.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "year",
        description = "Filters for name usages with a specific publication year.",
        schema = @Schema(implementation = Integer.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "origin",
        description = "Filters for name usages with a specific origin.",
        schema = @Schema(implementation = Origin.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "issue",
        description = "A specific indexing issue as defined in ChecklistBanks https://api.checklistbank.org/vocab/issue[Issue enum].",
        schema = @Schema(implementation = Issue.class),
        in = ParameterIn.QUERY
      )
    }
  )
  @interface NameUsageSearchParameters{}

  @Target({METHOD, ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Parameters(
    value = {
      @Parameter(
        name = "sortBy",
        description = "Determines the sort order of results. Defaults to RELEVANCE.",
        schema = @Schema(implementation = NameUsageRequest.SortBy.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "reverse",
        description = "If true, reverses the sort order.",
        schema = @Schema(implementation = Boolean.class),
        in = ParameterIn.QUERY
      )
    }
  )
  @interface SortParameters{}


  @Target({METHOD, ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Parameters(
    value = {
      @Parameter(
        name = "searchType",
        description = "The type of search to perform (e.g. FUZZY or WORDS).",
        schema = @Schema(implementation = NameUsageSearchRequest.SearchType.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "searchContent",
        description = "Restricts full-text search to specific fields. Defaults to scientific name only.",
        schema = @Schema(implementation = NameUsageSearchRequest.SearchContent.class),
        in = ParameterIn.QUERY
      )
    }
  )
  @interface SearchParameters{}

}
