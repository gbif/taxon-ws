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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.gbif.taxon.api.ChecklistMetrics;
import org.gbif.taxon.dao.DatasetKeyMap;
import org.gbif.taxon.dao.TaxonDao;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Checklist API",
            version = "v2",
            description = """
                tbd
                """,
            termsOfService = "https://www.gbif.org/terms"),
    servers = {
      @Server(url = "https://api.gbif.org/v2/", description = "Production"),
      @Server(url = "https://api.gbif-uat.org/v2/", description = "User testing")
    })
@Tag(name = "Checklist", description = "Checklist dataset indexed by ChecklistBank for GBIF")
@RequestMapping(value = "dataset", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class DatasetResource {

  private final TaxonDao dao;
  private final DatasetKeyMap keyMap;

  public DatasetResource(TaxonDao taxonDao, DatasetKeyMap keyMap) {
    this.dao = taxonDao;
    this.keyMap = keyMap;
  }

  @GetMapping("/{datasetKey}/metrics")
  public ChecklistMetrics get(
      @PathVariable("datasetKey")
      @Parameter(
          description = "UUID for the dataset key",
          example = "83a00190-7038-3970-a7e8-5e5563c40e37"
      )
      UUID datasetKey
    ) {
    return dao.metrics(datasetKey);
  }

  @DeleteMapping("/flush")
  public boolean flush() throws IOException {
    keyMap.flush();
    return true;
  }

}
