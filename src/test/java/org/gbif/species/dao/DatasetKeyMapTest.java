package org.gbif.species.dao;

import life.catalogue.api.model.DSID;
import life.catalogue.api.model.Dataset;
import life.catalogue.cache.LatestDatasetKeyCache;
import life.catalogue.db.mapper.DatasetMapper;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetKeyMapTest {

  @Mock
  private SqlSessionFactory factory;

  @Mock
  private SqlSession session;

  @Mock
  private DatasetMapper datasetMapper;

  @Mock
  private LatestDatasetKeyCache cache;

  private DatasetKeyMap map;

  @BeforeEach
  void setUp() {
    when(factory.openSession()).thenReturn(session);
    when(session.getMapper(DatasetMapper.class)).thenReturn(datasetMapper);
    map = new DatasetKeyMap(factory, cache);
  }

  @Test
  void toCLBSuccess() {
    var uuid = UUID.randomUUID();
    when(datasetMapper.getKeyByGBIF(uuid)).thenReturn(42);

    int result = map.toCLB(uuid);

    assertThat(result).isEqualTo(42);
  }

  @Test
  void toCLBUnknownUuidThrows() {
    var uuid = UUID.randomUUID();
    when(datasetMapper.getKeyByGBIF(uuid)).thenReturn(null);

    assertThatThrownBy(() -> map.toCLB(uuid))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Unknown dataset key");
  }

  @Test
  void toGBIFSuccess() {
    var uuid = UUID.randomUUID();
    var dataset = new Dataset();
    dataset.setKey(99);
    dataset.setGbifKey(uuid);
    when(datasetMapper.get(99)).thenReturn(dataset);

    UUID result = map.toGBIF(99);

    assertThat(result).isEqualTo(uuid);
  }

  @Test
  void toGBIFUnknownKeyThrows() {
    when(datasetMapper.get(999)).thenReturn(null);

    assertThatThrownBy(() -> map.toGBIF(999))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Unknown dataset key");
  }

  @Test
  void toDSIDCreatesCorrectDSID() {
    var uuid = UUID.randomUUID();
    when(datasetMapper.getKeyByGBIF(uuid)).thenReturn(101);

    DSID<String> dsid = map.toDSID(uuid, "taxon-123");

    assertThat(dsid.getDatasetKey()).isEqualTo(101);
    assertThat(dsid.getId()).isEqualTo("taxon-123");
  }

  @Test
  void bidirectionalCachePopulation() {
    var uuid = UUID.randomUUID();
    // When looking up by GBIF UUID, it should also populate the reverse cache
    when(datasetMapper.getKeyByGBIF(uuid)).thenReturn(42);

    // First call populates gbif2clb and also clb2gbif
    int clbKey = map.toCLB(uuid);
    assertThat(clbKey).isEqualTo(42);

    // Second call to toGBIF should use the cache populated by toCLB
    UUID result = map.toGBIF(42);
    assertThat(result).isEqualTo(uuid);
  }
}
