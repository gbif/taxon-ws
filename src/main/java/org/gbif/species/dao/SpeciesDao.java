package org.gbif.species.dao;

import life.catalogue.api.model.NameUsageBase;
import life.catalogue.db.mapper.NameUsageMapper;

import org.apache.ibatis.session.SqlSessionFactory;


import org.gbif.nameparser.api.Rank;
import org.gbif.species.api.NameUsage;


import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class SpeciesDao {
  private DatasetKeyMap map;
  private SqlSessionFactory factory;

  public SpeciesDao(DatasetKeyMap map, SqlSessionFactory factory) {
    this.map = map;
    this.factory = factory;
  }

  public NameUsage get(UUID uuid, String taxonKey) {
    return conv(getCLB(uuid, taxonKey));
  }

  public NameUsageBase getCLB(UUID uuid, String taxonKey) {
    try (var session = factory.openSession()) {
      var num = session.getMapper(NameUsageMapper.class);
      return num.get(map.toDSID(uuid, taxonKey));
    }
  }

  private NameUsage conv(NameUsageBase nub) {
    var nu = new NameUsage();
    nu.setDatasetKey(map.toGBIF(nub.getDatasetKey()));
    nu.setTaxonID(nub.getId());
    var n = nub.getName();
    nu.setTaxonRank(n.getRank());
    nu.setScientificName(n.getScientificName());
    return nu;
  }

}
