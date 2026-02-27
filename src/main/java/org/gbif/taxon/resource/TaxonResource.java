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

import life.catalogue.api.vocab.DatasetType;
import life.catalogue.common.io.UTF8IoUtils;
import life.catalogue.printer.JsonTreePrinter;


import org.gbif.taxon.api.NameUsage;
import org.gbif.taxon.api.NameUsageSimple;
import org.gbif.taxon.api.UsageInfo;
import org.gbif.taxon.dao.TaxonDao;


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
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

  public TaxonResource(TaxonDao searchService) {
    this.dao = searchService;
  }

  @GetMapping("/{uuid}/{taxonKey}")
  public NameUsageSimple get(
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
  public StreamingResponseBody breakdown(
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

    return os -> {
      try (Writer writer = UTF8IoUtils.writerFromStream(os);
           JsonTreePrinter printer = dao.childrenBreakdownPrinter(datasetKey, taxonKey, writer)
      ) {
        printer.print();
        writer.flush();
      }
    };
  }


  @GetMapping("/{uuid}/{taxonKey}/related")
  public List<NameUsageSimple> getRelated(
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

  @GetMapping("/search")
  public List<NameUsage> search() {
    // TODO: ES integration
    return List.of();
  }

  @GetMapping("/suggest")
  public List<NameUsageSimple> suggest() {
    // TODO: ES integration
    return List.of();
  }
}
