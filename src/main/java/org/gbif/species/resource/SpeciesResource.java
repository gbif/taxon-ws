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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import life.catalogue.api.model.NameUsageBase;

import life.catalogue.common.io.UTF8IoUtils;
import life.catalogue.printer.JsonTreePrinter;


import org.gbif.species.api.NameUsage;
import org.gbif.species.api.SimpleUsage;
import org.gbif.species.api.UsageInfo;
import org.gbif.species.dao.SpeciesDao;


import java.io.Writer;
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

  @GetMapping("/{uuid}/{taxonKey}/info")
  public UsageInfo getInfo(
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
    return dao.getInfo(uuid, taxonKey);
  }

  @GetMapping("/{uuid}/{taxonKey}/breakdown")
  public Response breakdown(
      @PathVariable("uuid")
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
    StreamingOutput stream = os -> {
      try (Writer writer = UTF8IoUtils.writerFromStream(os);
           JsonTreePrinter printer = dao.childrenBreakdownPrinter(datasetKey, taxonKey, writer)
      ) {
        printer.print();
        writer.flush();
      }
    };
    return Response.ok(stream).build();
  }


  @GetMapping("/{uuid}/{taxonKey}/related")
  public List<SimpleUsage> getRelated(
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
    String taxonKey,
    @RequestParam(required = false)
    @Parameter(
      description = "Optional type filter: 'treatments' or 'invasive'"
    )
    String type
  ) {
    return dao.getRelated(uuid, taxonKey, type);
  }

  @GetMapping("/search")
  public List<NameUsage> search() {
    // TODO: ES integration
    return List.of();
  }

  @GetMapping("/suggest")
  public List<SimpleUsage> suggest() {
    // TODO: ES integration
    return List.of();
  }
}
