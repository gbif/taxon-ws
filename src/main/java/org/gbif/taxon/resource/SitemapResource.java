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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import io.swagger.v3.oas.annotations.Hidden;
import life.catalogue.api.model.Page;
import life.catalogue.db.mapper.NameUsageMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.taxon.config.SitemapConfig;
import org.gbif.taxon.dao.DatasetKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Species sitemap resource producing text sitemaps for the COL checklist used to build GBIF species pages.
 * As sitemaps are limited to a maximum of 50k entries a sitemap index file is also dynamically created.
 *
 * see https://www.sitemaps.org/protocol.html
 */
@Hidden
@RequestMapping(value = "sitemap/taxon", produces = MediaType.TEXT_PLAIN_VALUE)
@RestController
public class SitemapResource {
  private static final Logger LOG = LoggerFactory.getLogger(SitemapResource.class);
  private static final int SITEMAP_SIZE = 50000;
  private static final String INDEX_TEMPLATE = "sitemapindex.ftl";
  private static final boolean INCL_SYNONYMS = false;
  private static final int MIN_LEN = 20;
  private final Configuration freemarker;
  private final SqlSessionFactory factory;
  private final int colKey;
  private final String portalTaxonUrl;
  private final String apiUrl;

  public SitemapResource(SqlSessionFactory factory, DatasetKeyMap keyMap, Configuration freemarker,
                         SitemapConfig cfg) {
    this.freemarker = freemarker;
    this.factory = factory;
    this.colKey = keyMap.getColKey();
    this.portalTaxonUrl = assertTrailingSlash(cfg.getPortal()) + "taxon/";
    this.apiUrl = assertTrailingSlash(cfg.getApi());
  }

  private static String assertTrailingSlash(String url) {
    if (!url.endsWith("/")) {
      return url + "/";
    }
    return url;
  }

  /**
   * Generate a sitemap index to all sitemaps.
   */
  @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<?> sitemapIndex() throws IOException {
    try (SqlSession session = factory.openSession()) {
      var num = session.getMapper(NameUsageMapper.class);
      int cnt = num.countIds(colKey, INCL_SYNONYMS, MIN_LEN);
      int maps = (int) Math.ceil((double) cnt/SITEMAP_SIZE);
      LOG.info("Requested sitemap index to {} index files with {} usages", maps, cnt);

      try (Writer writer = new StringWriter()) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("apiUrl", apiUrl);
        data.put("maps", maps);
        data.put("cnt", cnt);
        freemarker.getTemplate(INDEX_TEMPLATE).process(data, writer);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(writer.toString());

      } catch (TemplateException e) {
        throw new IOException("Error while processing the sitemapindex template", e);
      }
    }
  }

  /**
   * Generate a single text sitemap with 50k entries.
   */
  @GetMapping(path="{page}.txt")
  public ResponseEntity<StreamingResponseBody> sitemap(@PathVariable("page") int page) {
    Preconditions.checkArgument(page > 0, "Page parameter must be positive");

    StreamingResponseBody stream = os -> {
      Writer writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
      // we cannot use the constructor properly as our limit is larger than allowed :)
      Page req = new Page((page - 1) * SITEMAP_SIZE, 10);
      req.setLimit(SITEMAP_SIZE);
      try (SqlSession session = factory.openSession()) {
        var num = session.getMapper(NameUsageMapper.class);
        for (String key : num.pageIds(colKey, INCL_SYNONYMS, MIN_LEN, req)) {
          writer.write(portalTaxonUrl);
          writer.write(key);
          writer.write("\n");
        }
      }
      writer.flush();
    };

    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body(stream);
  }
}
