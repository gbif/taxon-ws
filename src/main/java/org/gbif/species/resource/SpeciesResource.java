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
package org.gbif.species.resource;

import life.catalogue.api.model.NameUsageBase;


import org.gbif.species.api.NameUsage;
import org.gbif.species.dao.SpeciesDao;


import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@RequestMapping(value = "species", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class SpeciesResource {
  private static final Logger LOG = LoggerFactory.getLogger(SpeciesResource.class);

  private final SpeciesDao dao;

  public SpeciesResource(SpeciesDao searchService) {
    this.dao = searchService;
  }

  @GetMapping("/{uuid}/{taxonKey}")
  public NameUsage get(
      @PathVariable("uuid")
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

  @GetMapping("/{uuid}/{taxonKey}/_orig")
  public NameUsageBase getCLB(
    @PathVariable("uuid")
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
    return dao.getCLB(uuid, taxonKey);
  }

}
