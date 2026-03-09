package org.gbif.taxon.config;

import lombok.Data;

import java.util.UUID;

@Data
public class DatasetKeys {
  private UUID iucn;
  private UUID cites;
}
