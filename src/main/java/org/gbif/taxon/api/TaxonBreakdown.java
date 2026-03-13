package org.gbif.taxon.api;

import lombok.Data;


import org.gbif.nameparser.api.Rank;


import java.util.List;

@Data
public class TaxonBreakdown {
  private String taxonID;
  private Rank taxonRank;
  private String scientificName;
  private int species;
  private List<TaxonBreakdown> breakdown;

}
