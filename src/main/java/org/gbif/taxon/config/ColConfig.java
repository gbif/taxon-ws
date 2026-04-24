package org.gbif.taxon.config;

import lombok.Data;
import org.gbif.api.model.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@Component
@ConfigurationProperties(prefix = "col")
public class ColConfig {
  private String matchingUrl;
  private Integer datasetKey;
  private UUID baseRelease     = UUID.fromString("e007cc4a-8704-449d-8829-bb209d26d6c8");
  private UUID extendedRelease = Constants.COL_DATASET_KEY;

}
