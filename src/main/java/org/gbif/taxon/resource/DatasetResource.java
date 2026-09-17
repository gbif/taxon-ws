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
import org.gbif.taxon.api.ChecklistMetrics;
import org.gbif.taxon.dao.TaxonDao;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * The former home of the checklist metrics, kept alive until all clients, the GBIF portal in
 * particular, have moved to {@link ChecklistResource} at taxon/checklist.
 * The dataset path cannot stay, as it would clash with the registry dataset resource once the
 * experimental prefix is dropped from the v2 API.
 */
@Tag(name = "Checklist")
@RequestMapping(value = "dataset", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@Deprecated
public class DatasetResource {

  private final TaxonDao dao;

  public DatasetResource(TaxonDao taxonDao) {
    this.dao = taxonDao;
  }

  @Deprecated
  @Operation(
    operationId = "getDatasetMetrics",
    summary = "Get import metrics for a checklist dataset",
    description = "Deprecated, use /taxon/checklist/{datasetKey}/metrics instead. " +
      "Returns counts and statistics from the latest successful import of the given checklist dataset.",
    deprecated = true
  )
  @ApiResponse(responseCode = "200", description = "Dataset metrics")
  @ApiResponse(responseCode = "404", description = "Dataset not found")
  @GetMapping("/{datasetKey}/metrics")
  public ChecklistMetrics metrics(
      @PathVariable("datasetKey")
      @Parameter(
          description = "UUID for the dataset key",
          example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
      )
      UUID datasetKey
    ) {
    return dao.metrics(datasetKey);
  }

}
