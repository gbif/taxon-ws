package org.gbif.taxon.dao;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.model.DSID;
import life.catalogue.api.model.Dataset;
import life.catalogue.cache.LatestDatasetKeyCache;
import life.catalogue.db.mapper.DatasetMapper;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.taxon.config.ColConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatasetKeyMapTest {

  @Mock
  private SqlSessionFactory factory;

  @Mock
  private SqlSession session;

  @Mock
  private DatasetMapper datasetMapper;

  @Mock
  private LatestDatasetKeyCache cache;

  @Mock
  private JsonFetcher jsonFetcher;

  private DatasetKeyMap map;

  @BeforeEach
  void setUp() throws IOException {
    when(factory.openSession()).thenReturn(session);
    when(session.getMapper(DatasetMapper.class)).thenReturn(datasetMapper);
    JsonNode json = new ObjectMapper().readValue("""
    {"mainIndex": {"clbDatasetKey": 1234}}
    """, JsonNode.class);
    when(jsonFetcher.fetchJson(any())).thenReturn(json);
    var cfg = new ColConfig();
    cfg.setMatchingUrl("http://localhost:8080/matcher-ws"); // this is ignored
    map = new DatasetKeyMap(factory, cache, jsonFetcher, cfg);
  }

  @Test
  void retrieveCurrentColXRKey() throws IOException {
    int result = map.retrieveCurrentColXRKey();
    assertThat(result).isEqualTo(1234);
  }

  @Test
  void refreshColKeyUnchanged() {
    assertThat(map.getColKey()).isEqualTo(1234);
    assertThat(map.refreshColKey()).isFalse();
    assertThat(map.getColKey()).isEqualTo(1234);
  }

  @Test
  void refreshColKeyChanged() throws IOException {
    JsonNode json = new ObjectMapper().readValue("""
    {"mainIndex": {"clbDatasetKey": 5678}}
    """, JsonNode.class);
    when(jsonFetcher.fetchJson(any())).thenReturn(json);

    assertThat(map.refreshColKey()).isTrue();
    assertThat(map.getColKey()).isEqualTo(5678);
  }

  @Test
  void refreshColKeyKeepsOldKeyOnFailure() {
    when(jsonFetcher.fetchJson(any())).thenThrow(new IllegalStateException("matcher is down"));

    assertThatThrownBy(map::refreshColKey).isInstanceOf(IllegalStateException.class);
    assertThat(map.getColKey()).isEqualTo(1234);
  }

  @Test
  void refreshColKeyIgnoredWhenFixed() throws IOException {
    var cfg = new ColConfig();
    cfg.setDatasetKey(4242);
    var fixed = new DatasetKeyMap(factory, cache, jsonFetcher, cfg);

    assertThat(fixed.getColKey()).isEqualTo(4242);
    assertThat(fixed.refreshColKey()).isFalse();
    assertThat(fixed.getColKey()).isEqualTo(4242);
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
      .isInstanceOf(NotFoundException.class)
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
      .isInstanceOf(MissingGBIFKeyException.class)
      .hasMessageContaining("GBIF registry entry missing for CLB dataset 999");
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
