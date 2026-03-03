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
import life.catalogue.api.search.NameUsageRequest;
import life.catalogue.api.vocab.*;
import life.catalogue.common.io.UTF8IoUtils;
import life.catalogue.printer.JsonTreePrinter;


import org.gbif.api.documentation.CommonParameters;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.NomCode;
import org.gbif.nameparser.api.Rank;
import org.gbif.taxon.api.ChecklistMetrics;
import org.gbif.taxon.api.NameUsageSimple;
import org.gbif.taxon.api.UsageInfo;
import org.gbif.taxon.api.search.*;
import org.gbif.taxon.dao.TaxonDao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.Writer;
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
  private static final Logger LOG = LoggerFactory.getLogger(DatasetResource.class);

  private final TaxonDao dao;

  public DatasetResource(TaxonDao taxonDao) {
    this.dao = taxonDao;
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
}
