package org.gbif.taxon.api;

import java.util.List;

import lombok.Data;

@Data
public class Synonymy {
  private List<NameUsageSimple> homotypic;
  private List<List<NameUsageSimple>> heterotypic;
  private List<NameUsageSimple> misapplied;

  public int size() {
    return this.homotypic.size() + this.heterotypic.size() + this.misapplied.size();
  }

}
