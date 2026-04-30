package org.gbif.taxon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sitemap")
public class SitemapConfig {
  private String api;
  private String portal;
}
