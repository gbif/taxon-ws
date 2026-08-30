package org.gbif.taxon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * The curated list of taxonomies GBIF occurrences are indexed against
 * and the ChecklistBank locations their data can be retrieved from.
 */
@Data
@Component
@ConfigurationProperties(prefix = "checklists")
public class ChecklistConfig {

  private List<UUID> keys = List.of(
    UUID.fromString("d7dddbf4-2cf0-4f39-9b2a-bb099caae36c"), // GBIF Backbone Taxonomy
    UUID.fromString("7ddf754f-d193-4cc9-b351-99906754a03b") // Catalogue of Life, extended release
  );

  private String clbApi = "https://api.checklistbank.org/";
  private String clbUi = "https://www.checklistbank.org/";

  /**
   * @return the ChecklistBank landing page for the given ChecklistBank dataset key
   */
  public URI checklistBankUrl(int clbKey) {
    return URI.create(assertTrailingSlash(clbUi) + "dataset/" + clbKey + "/about");
  }

  /**
   * @return the ChecklistBank export URL for the given ChecklistBank dataset key and data format
   */
  public URI downloadUrl(int clbKey, String format) {
    return URI.create(assertTrailingSlash(clbApi) + "dataset/" + clbKey + "/export.zip?format=" + format + "&extended=true");
  }

  private static String assertTrailingSlash(String url) {
    return url.endsWith("/") ? url : url + "/";
  }
}
