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

import life.catalogue.api.search.NameUsageRequest;
import life.catalogue.api.vocab.NameField;
import life.catalogue.api.vocab.TaxGroup;


import org.gbif.api.documentation.CommonParameters;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.nameparser.api.NomCode;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.Rank;

import org.gbif.taxon.api.NameUsageSimple;
import org.gbif.taxon.api.UsageInfo;
import org.gbif.taxon.api.search.BaseNameUsageRequest;
import org.gbif.taxon.api.search.NameUsageSearchParameter;
import org.gbif.taxon.api.search.NameUsageSearchRequest;
import org.gbif.taxon.api.search.NameUsageSearchResult;
import org.gbif.taxon.api.search.NameUsageSuggestRequest;
import org.gbif.taxon.api.search.NameUsageSuggestResult;
import org.gbif.taxon.dao.TaxonDao;


import java.io.Writer;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import life.catalogue.api.vocab.DatasetType;
import life.catalogue.api.vocab.Issue;
import life.catalogue.api.vocab.Origin;
import life.catalogue.api.vocab.NomStatus;
import life.catalogue.api.vocab.TaxonomicStatus;

import life.catalogue.common.io.UTF8IoUtils;
import life.catalogue.printer.JsonTreePrinter;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Species API",
            version = "v2",
            description = """
                This API powers the [Species v2 resources](https://www.gbif.org/resource/search?contentType=species) on GBIF.org.","
                """,
            termsOfService = "https://www.gbif.org/terms"),
    servers = {
      @Server(url = "https://api.gbif.org/v2/", description = "Production"),
      @Server(url = "https://api.gbif-uat.org/v2/", description = "User testing")
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

  @GetMapping("/{datasetKey}/{taxonKey}")
  public NameUsageSimple get(
      @PathVariable("datasetKey")
      @Parameter(
          description = "UUID for the dataset key",
          example = "83a00190-7038-3970-a7e8-5e5563c40e37"
      )
      UUID uuid,
      @PathVariable("taxonKey")
      @Parameter(
        description = "Taxon key scoped within the dataset",
        example = "CXA"
      )
      String taxonKey
    ) {
    return dao.get(uuid, taxonKey);
  }

  @GetMapping("/{datasetKey}/{taxonKey}/info")
  public UsageInfo getInfo(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "83a00190-7038-3970-a7e8-5e5563c40e37"
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

  @GetMapping("/{datasetKey}/{taxonKey}/breakdown")
  public StreamingResponseBody breakdown(
      @PathVariable("datasetKey")
      @Parameter(
        description = "UUID for the dataset key",
        example = "83a00190-7038-3970-a7e8-5e5563c40e37"
      )
      UUID datasetKey,
      @PathVariable("taxonKey")
      @Parameter(
        description = "Taxon key scoped within the dataset",
        example = "CXA"
      )
      String taxonKey
    ) {

    return os -> {
      try (Writer writer = UTF8IoUtils.writerFromStream(os);
           JsonTreePrinter printer = dao.childrenBreakdownPrinter(datasetKey, taxonKey, writer)
      ) {
        printer.print();
        writer.flush();
      }
    };
  }


  @GetMapping("/{datasetKey}/{taxonKey}/related")
  public List<NameUsageSimple> getRelated(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "83a00190-7038-3970-a7e8-5e5563c40e37"
    )
    UUID uuid,
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
      description = "Optional dataset key filter, repeatable: e.g. `?datasetKey=1&datasetKey=2`",
      explode = Explode.TRUE,
      array = @ArraySchema(schema = @Schema(type = "integer"))
    )
    List<Integer> datasetKeys,
    @RequestParam(name = "publisherKey", required = false)
    @Parameter(
      description = "Optional publisher key filter, repeatable: e.g. `?publisherKey=<uuid>&publisherKey=<uuid>`",
      explode = Explode.TRUE,
      array = @ArraySchema(schema = @Schema(type = "string", format = "uuid"))
    )
    List<UUID> publisherKeys
  ) {
    return dao.listRelated(uuid, taxonKey, datasetTypes, datasetKeys, publisherKeys);
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
  @Parameter(
    name = "request",
    hidden = true
  )
  @ApiResponse(responseCode = "200", description = "Name usages found")
  @GetMapping("/search/{datasetKey}")
  public SearchResponse<NameUsageSearchResult, NameUsageSearchParameter> search(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "83a00190-7038-3970-a7e8-5e5563c40e37"
    )
    UUID datasetKey,
    NameUsageSearchRequest request
  ) {
    setDatasetKey(request, datasetKey);
    return dao.search(request);
  }

  private static void setDatasetKey(BaseNameUsageRequest request, UUID datasetKey) {
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
  @Parameter(
    name = "request",
    hidden = true
  )
  @ApiResponse(responseCode = "200", description = "Name usages found")
  @GetMapping("/suggest/{datasetKey}")
  public List<NameUsageSuggestResult> suggest(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "83a00190-7038-3970-a7e8-5e5563c40e37"
    )
    UUID datasetKey,
    NameUsageSuggestRequest request
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
        name = "datasetKey",
        description = "A UUID of a checklist dataset.",
        schema = @Schema(implementation = UUID.class),
        example = "d7dddbf4-2cf0-4f39-9b2a-bb099caae36c",
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "rank",
        description = "Filters by taxonomic rank.",
        schema = @Schema(implementation = Rank.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "taxonID",
        description = "Filters by any of the higher Linnean rank keys. Note this is within the respective checklist " +
          "and not searching NUB keys across all checklists.",
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
        name = "nameType",
        description = "Filters by the name type as given in our https://api.gbif.org/v1/enumeration/basic/NameType[NameType enum].",
        schema = @Schema(implementation = NameType.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "code",
        description = "Filters by the nomenclatural code.",
        schema = @Schema(implementation = NomCode.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "nomStatus",
        description = "Filters by the nomenclatural status as given in our https://api.gbif.org/v1/enumeration/basic/NomenclaturalStatus[Nomenclatural Status enum].",
        schema = @Schema(implementation = NomStatus.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "origin",
        description = "Filters for name usages with a specific origin.",
        schema = @Schema(implementation = Origin.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "group",
        description = "Filters for name usages with a specific taxonomic group.",
        schema = @Schema(implementation = TaxGroup.class),
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
        name = "field",
        description = "Filters for name usages with a specific field of the parsed name to be present.",
        schema = @Schema(implementation = NameField.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "issue",
        description = "A specific indexing issue as defined in our https://api.gbif.org/v1/enumeration/basic/NameUsageIssue[NameUsageIssue enum].",
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
        description = "Filters for name usages with a specific field of the parsed name to be present.",
        schema = @Schema(implementation = NameUsageRequest.SortBy.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "reverse",
        description = "Filters for name usages with a specific field of the parsed name to be present.",
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
        name = "datasetKey",
        schema = @Schema(implementation = NameUsageRequest.SearchType.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "searchType",
        schema = @Schema(implementation = NameUsageRequest.SearchType.class),
        in = ParameterIn.QUERY
      ),
      @Parameter(
        name = "content",
        schema = @Schema(implementation = NameUsageRequest.SearchContent.class),
        in = ParameterIn.QUERY
      )
    }
  )
  @interface SearchParameters{}

}
