package org.gbif.species.api;

import lombok.Getter;
import lombok.Setter;


import org.gbif.nameparser.api.Rank;


import java.util.UUID;

@Getter
@Setter
public class NameUsage {
  private UUID datasetKey;
  private String key;
  private Rank rank;
  private String scientificName;
}
