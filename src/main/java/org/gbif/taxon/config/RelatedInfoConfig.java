package org.gbif.taxon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@Component
@ConfigurationProperties(prefix = "related")
public class RelatedInfoConfig {
  private UUID iucn = UUID.fromString("19491596-35ae-4a91-9a98-85cf505f1bd3");
  private Integer citesI = 314512;
  private Integer citesII = 314531;
  private Integer citesIII = 314533;
  private UUID griisPublisherKey = UUID.fromString("cdef28b1-db4e-4c58-aa71-3c5238c2d0b5");

  public boolean isCites(Integer datasetKey) {
    return datasetKey != null && (
      datasetKey.equals(citesI) || datasetKey.equals(citesII) || datasetKey.equals(citesIII)
    );
  }
}
