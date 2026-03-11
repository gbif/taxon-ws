package org.gbif.taxon.dao;

public class MissingGBIFKeyException extends RuntimeException {
  private final int datasetKey;

  public MissingGBIFKeyException(int datasetKey) {
    super("GBIF registry entry missing for CLB dataset " + datasetKey);
    this.datasetKey = datasetKey;
  }

  public int getDatasetKey() {
    return datasetKey;
  }
}
