package org.gbif.taxon.dao;

import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.model.DOI;
import life.catalogue.api.model.Dataset;
import life.catalogue.common.date.FuzzyDate;
import life.catalogue.db.mapper.DatasetMapper;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.taxon.config.ChecklistConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChecklistDaoTest {

  private static final UUID COL = UUID.fromString("7ddf754f-d193-4cc9-b351-99906754a03b");
  private static final UUID WORMS = UUID.fromString("2d59e5db-57ad-41ff-97d6-11f5fb264527");

  @Mock
  private SqlSessionFactory factory;

  @Mock
  private SqlSession session;

  @Mock
  private DatasetMapper datasetMapper;

  @Mock
  private DatasetKeyMap keyMap;

  private ChecklistConfig cfg;
  private ChecklistDao dao;

  @BeforeEach
  void setUp() {
    when(factory.openSession()).thenReturn(session);
    when(session.getMapper(DatasetMapper.class)).thenReturn(datasetMapper);
    cfg = new ChecklistConfig();
    cfg.setKeys(List.of(COL, WORMS));
    dao = new ChecklistDao(keyMap, factory, cfg);
  }

  private static Dataset col() {
    var d = new Dataset();
    d.setKey(315557);
    d.setAlias("COL26.6 XR");
    d.setTitle("Catalogue of Life");
    d.setVersion("June 2026");
    d.setIssued(FuzzyDate.of(2026, 6, 15));
    d.setDoi(new DOI("10.48580/dgy8b"));
    d.setVersionDoi(new DOI("10.48580/dgxsq"));
    d.setUrl(URI.create("https://www.catalogueoflife.org"));
    d.setLogo(URI.create("https://api.checklistbank.org/dataset/315557/logo"));
    d.setSize(7991756);
    return d;
  }

  @Test
  void getConvertsAllFields() {
    when(keyMap.toCLB(COL)).thenReturn(315557);
    when(keyMap.toGBIF(315557)).thenReturn(COL);
    when(datasetMapper.get(315557)).thenReturn(col());

    var c = dao.get(COL);

    assertThat(c.getKey()).isEqualTo(COL);
    assertThat(c.getChecklistBankKey()).isEqualTo(315557);
    assertThat(c.getAlias()).isEqualTo("COL26.6 XR");
    assertThat(c.getTitle()).isEqualTo("Catalogue of Life");
    assertThat(c.getVersion()).isEqualTo("June 2026");
    assertThat(c.getIssued()).isEqualTo("2026-06-15");
    assertThat(c.getDoi()).isEqualTo("10.48580/dgy8b");
    assertThat(c.getVersionDoi()).isEqualTo("10.48580/dgxsq");
    assertThat(c.getUrl()).isEqualTo(URI.create("https://www.catalogueoflife.org"));
    assertThat(c.getUsageCount()).isEqualTo(7991756);
    assertThat(c.getChecklistBankUrl())
      .isEqualTo(URI.create("https://www.checklistbank.org/dataset/315557/about"));
    assertThat(c.getColdpDownloadUrl())
      .isEqualTo(URI.create("https://api.checklistbank.org/dataset/315557/export.zip?format=ColDP&extended=true"));
    assertThat(c.getDwcaDownloadUrl())
      .isEqualTo(URI.create("https://api.checklistbank.org/dataset/315557/export.zip?format=DwCA&extended=true"));
  }

  @Test
  void getWithoutOptionalFields() {
    var d = new Dataset();
    d.setKey(42);
    d.setTitle("Bare bones");
    when(keyMap.toCLB(WORMS)).thenReturn(42);
    when(keyMap.toGBIF(42)).thenReturn(WORMS);
    when(datasetMapper.get(42)).thenReturn(d);

    var c = dao.get(WORMS);

    assertThat(c.getTitle()).isEqualTo("Bare bones");
    assertThat(c.getIssued()).isNull();
    assertThat(c.getDoi()).isNull();
    assertThat(c.getVersionDoi()).isNull();
    assertThat(c.getUsageCount()).isNull();
  }

  @Test
  void getUnknownDatasetThrows() {
    when(keyMap.toCLB(WORMS)).thenReturn(42);
    when(datasetMapper.get(42)).thenReturn(null);

    assertThatThrownBy(() -> dao.get(WORMS)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void listKeepsConfiguredOrder() {
    var worms = new Dataset();
    worms.setKey(2011);
    worms.setTitle("World Register of Marine Species");
    when(keyMap.toCLB(COL)).thenReturn(315557);
    when(keyMap.toGBIF(315557)).thenReturn(COL);
    when(datasetMapper.get(315557)).thenReturn(col());
    when(keyMap.toCLB(WORMS)).thenReturn(2011);
    when(keyMap.toGBIF(2011)).thenReturn(WORMS);
    when(datasetMapper.get(2011)).thenReturn(worms);

    var list = dao.list();

    assertThat(list).hasSize(2);
    assertThat(list.get(0).getKey()).isEqualTo(COL);
    assertThat(list.get(1).getKey()).isEqualTo(WORMS);
  }

  @Test
  void listSkipsUnresolvableChecklists() {
    when(keyMap.toCLB(COL)).thenThrow(new NotFoundException(COL, "Unknown dataset key: " + COL));
    when(keyMap.toCLB(WORMS)).thenReturn(2011);
    when(keyMap.toGBIF(2011)).thenThrow(new MissingGBIFKeyException(2011));

    var list = dao.list();

    assertThat(list).isEmpty();
  }
}
