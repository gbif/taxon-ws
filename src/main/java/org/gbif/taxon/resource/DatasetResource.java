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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.gbif.taxon.api.ChecklistMetrics;
import org.gbif.taxon.dao.DatasetKeyMap;
import org.gbif.taxon.dao.TaxonDao;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "Checklist", description = "Checklist dataset operations — import metrics and cache management")
@RequestMapping(value = "dataset", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class DatasetResource {

  private final TaxonDao dao;
  private final DatasetKeyMap keyMap;

  public DatasetResource(TaxonDao taxonDao, DatasetKeyMap keyMap) {
    this.dao = taxonDao;
    this.keyMap = keyMap;
  }

  @Operation(
    operationId = "getDatasetMetrics",
    summary = "Get import metrics for a checklist dataset",
    description = "Returns counts and statistics from the latest successful import of the given checklist dataset, " +
      "including record counts by type (taxa, synonyms, vernacular names, distributions, media, references) " +
      "and breakdowns by rank, nomenclatural code, name type, taxonomic status, and origin."
  )
  @ApiResponse(responseCode = "200", description = "Dataset metrics")
  @ApiResponse(responseCode = "404", description = "Dataset not found")
  @GetMapping("/{datasetKey}/metrics")
  public ChecklistMetrics get(
      @PathVariable("datasetKey")
      @Parameter(
          description = "UUID for the dataset key",
          example = "2d59e5db-57ad-41ff-97d6-11f5fb264527"
      )
      UUID datasetKey
    ) {
    return dao.metrics(datasetKey);
  }

  @Hidden
  @Operation(
    operationId = "flushDatasetCache",
    summary = "Flush the dataset key map cache",
    description = "Clears the internal dataset key map cache and the DAO cache. Admin/internal use only."
  )
  @ApiResponse(responseCode = "200", description = "Cache flushed successfully")
  @DeleteMapping("/flush")
  public boolean flush() throws IOException {
    keyMap.flush();
    dao.flushCache();
    return true;
  }

}
