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
import org.gbif.taxon.dao.DatasetKeyMap;
import org.gbif.taxon.dao.TaxonDao;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Hidden
@RequestMapping(value = "keymap", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class MetadataResource {
  private final DatasetKeyMap keyMap;
  private final TaxonDao dao;

  public MetadataResource(TaxonDao taxonDao, DatasetKeyMap keyMap) {
    this.dao = taxonDao;
    this.keyMap = keyMap;
  }

  @GetMapping
  public Map<UUID, Integer> meta() throws IOException {
    return keyMap.bimap();
  }

  @GetMapping("col")
  public int col() throws IOException {
    return keyMap.getColKey();
  }

  @DeleteMapping("/flush")
  public boolean flush() throws IOException {
    keyMap.flush();
    dao.flushCache();
    return true;
  }

}
