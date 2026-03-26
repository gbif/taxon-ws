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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.taxon.api.TreeUsage;
import org.gbif.taxon.dao.TaxonDao;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tree", description = "Taxonomic tree navigation")
@RequestMapping(value = "taxon/tree", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class TreeResource {

  private final TaxonDao dao;

  public TreeResource(TaxonDao dao) {
    this.dao = dao;
  }

  @Operation(
    operationId = "getRootTaxa",
    summary = "Get root taxa of a dataset",
    description = "Returns the top-level (root) accepted taxa of the given checklist dataset, i.e. taxa with no parent. " +
      "Results are paginated."
  )
  @ApiResponse(responseCode = "200", description = "Root taxa")
  @Pageable.OffsetLimitParameters
  @GetMapping("/{datasetKey}")
  public PagingResponse<TreeUsage> root(
    @PathVariable("datasetKey")
    @Parameter(
      description = "UUID for the dataset key",
      example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
    )
    UUID datasetKey,
    @Parameter(hidden = true) Pageable page
  ) {
    return dao.root(datasetKey, page);
  }

  @Operation(
    operationId = "getClassification",
    summary = "Get the classification path for a taxon",
    description = "Returns the ordered list of ancestor taxa from the root of the classification down to the given taxon, " +
      "inclusive of the taxon itself."
  )
  @ApiResponse(responseCode = "200", description = "Classification path")
  @ApiResponse(responseCode = "404", description = "Taxon not found")
  @GetMapping("/{datasetKey}/{taxonKey}")
  public List<TreeUsage> classification(
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
    return dao.classification(datasetKey, taxonKey);
  }

  @Operation(
    operationId = "getChildren",
    summary = "Get direct children of a taxon",
    description = "Returns the direct accepted child taxa of the given taxon. " +
      "Synonyms are excluded. Results are paginated."
  )
  @ApiResponse(responseCode = "200", description = "Child taxa")
  @ApiResponse(responseCode = "404", description = "Taxon not found")
  @Pageable.OffsetLimitParameters
  @GetMapping("/{datasetKey}/{taxonKey}/children")
  public PagingResponse<TreeUsage> children(
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
    String taxonKey,
    @Parameter(hidden = true) Pageable page
  ) {
    return dao.children(datasetKey, taxonKey, page);
  }
}
