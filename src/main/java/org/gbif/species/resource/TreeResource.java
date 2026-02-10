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

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.species.api.TreeUsage;
import org.gbif.species.dao.SpeciesDao;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tree", description = "Taxonomic tree navigation")
@RequestMapping(value = "taxon/tree", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class TreeResource {

  private final SpeciesDao dao;

  public TreeResource(SpeciesDao dao) {
    this.dao = dao;
  }

  @GetMapping("/{uuid}")
  public PagingResponse<TreeUsage> root(
    @PathVariable("uuid")
    @Parameter(
      description = "UUID for the dataset key",
      example = "83a00190-7038-3970-a7e8-5e5563c40e37"
    )
    UUID uuid,
    Pageable page
  ) {
    return dao.root(uuid, page);
  }

  @GetMapping("/{uuid}/{taxonKey}")
  public List<TreeUsage> classification(
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
    return dao.classification(uuid, taxonKey);
  }

  @GetMapping("/{uuid}/{taxonKey}/children")
  public PagingResponse<TreeUsage> children(
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
    Pageable page
  ) {
    return dao.children(uuid, taxonKey, page);
  }
}
