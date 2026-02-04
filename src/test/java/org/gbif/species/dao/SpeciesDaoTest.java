package org.gbif.species.dao;

import life.catalogue.api.model.DSID;
import life.catalogue.api.model.Name;
import life.catalogue.api.model.Synonym;
import life.catalogue.api.model.Taxon;
import life.catalogue.api.vocab.Origin;
import life.catalogue.db.mapper.DistributionMapper;
import life.catalogue.db.mapper.MediaMapper;
import life.catalogue.db.mapper.NameUsageMapper;
import life.catalogue.db.mapper.ReferenceMapper;
import life.catalogue.db.mapper.SynonymMapper;
import life.catalogue.db.mapper.TaxonPropertyMapper;
import life.catalogue.db.mapper.VernacularNameMapper;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.nameparser.api.Rank;
import org.gbif.species.api.NameUsage;
import org.gbif.species.api.SimpleUsage;
import org.gbif.species.api.UsageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import life.catalogue.api.vocab.TaxonomicStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpeciesDaoTest {

  @Mock
  private DatasetKeyMap map;

  @Mock
  private ApiConverter converter;

  @Mock
  private SqlSessionFactory factory;

  @Mock
  private SqlSession session;

  @Mock
  private NameUsageMapper nameUsageMapper;

  @Mock
  private VernacularNameMapper vernacularNameMapper;

  @Mock
  private DistributionMapper distributionMapper;

  @Mock
  private MediaMapper mediaMapper;

  @Mock
  private TaxonPropertyMapper taxonPropertyMapper;

  @Mock
  private SynonymMapper synonymMapper;

  @Mock
  private ReferenceMapper referenceMapper;

  private SpeciesDao dao;

  @BeforeEach
  void setUp() {
    lenient().when(factory.openSession()).thenReturn(session);
    lenient().when(session.getMapper(NameUsageMapper.class)).thenReturn(nameUsageMapper);
    dao = new SpeciesDao(map, converter, factory);
  }

  @Test
  void getReturnsConvertedUsage() {
    var uuid = UUID.randomUUID();
    var dsid = DSID.of(101, "t-1");
    when(map.toDSID(uuid, "t-1")).thenReturn(dsid);

    var taxon = createTaxon("t-1", TaxonomicStatus.ACCEPTED);
    when(nameUsageMapper.get(dsid)).thenReturn(taxon);

    var expectedNu = new NameUsage();
    expectedNu.setTaxonID("t-1");
    when(converter.convert(taxon)).thenReturn(expectedNu);

    NameUsage result = dao.get(uuid, "t-1");

    assertThat(result).isEqualTo(expectedNu);
  }

  @Test
  void getInfoForAcceptedTaxonQueriesAllMappers() {
    var uuid = UUID.randomUUID();
    var dsid = DSID.of(101, "t-1");
    when(map.toDSID(uuid, "t-1")).thenReturn(dsid);

    var taxon = createTaxon("t-1", TaxonomicStatus.ACCEPTED);
    when(nameUsageMapper.get(dsid)).thenReturn(taxon);

    when(session.getMapper(VernacularNameMapper.class)).thenReturn(vernacularNameMapper);
    when(session.getMapper(DistributionMapper.class)).thenReturn(distributionMapper);
    when(session.getMapper(MediaMapper.class)).thenReturn(mediaMapper);
    when(session.getMapper(TaxonPropertyMapper.class)).thenReturn(taxonPropertyMapper);
    when(session.getMapper(SynonymMapper.class)).thenReturn(synonymMapper);

    when(vernacularNameMapper.listByTaxon(dsid)).thenReturn(List.of());
    when(distributionMapper.listByTaxon(dsid)).thenReturn(List.of());
    when(mediaMapper.listByTaxon(dsid)).thenReturn(List.of());
    when(taxonPropertyMapper.listByTaxon(dsid)).thenReturn(List.of());
    when(synonymMapper.listByTaxon(dsid)).thenReturn(List.of());

    var expectedInfo = new UsageInfo();
    when(converter.convert(any(life.catalogue.api.model.UsageInfo.class))).thenReturn(expectedInfo);

    UsageInfo result = dao.getInfo(uuid, "t-1");

    assertThat(result).isEqualTo(expectedInfo);
    verify(vernacularNameMapper).listByTaxon(dsid);
    verify(distributionMapper).listByTaxon(dsid);
    verify(mediaMapper).listByTaxon(dsid);
    verify(taxonPropertyMapper).listByTaxon(dsid);
    verify(synonymMapper).listByTaxon(dsid);
  }

  @Test
  void getInfoForSynonymSkipsTaxonOnlyMappers() {
    var uuid = UUID.randomUUID();
    var dsid = DSID.of(101, "s-1");
    when(map.toDSID(uuid, "s-1")).thenReturn(dsid);

    var synonym = createSynonym("s-1");
    when(nameUsageMapper.get(dsid)).thenReturn(synonym);

    when(session.getMapper(SynonymMapper.class)).thenReturn(synonymMapper);
    when(synonymMapper.listByTaxon(dsid)).thenReturn(List.of());

    var expectedInfo = new UsageInfo();
    when(converter.convert(any(life.catalogue.api.model.UsageInfo.class))).thenReturn(expectedInfo);

    UsageInfo result = dao.getInfo(uuid, "s-1");

    assertThat(result).isEqualTo(expectedInfo);
    // synonym should NOT query these mappers
    verify(session, never()).getMapper(VernacularNameMapper.class);
    verify(session, never()).getMapper(DistributionMapper.class);
    verify(session, never()).getMapper(MediaMapper.class);
    verify(session, never()).getMapper(TaxonPropertyMapper.class);
  }

  @Test
  void getInfoReturnsNullWhenUsageNotFound() {
    var uuid = UUID.randomUUID();
    var dsid = DSID.of(101, "t-missing");
    when(map.toDSID(uuid, "t-missing")).thenReturn(dsid);
    when(nameUsageMapper.get(dsid)).thenReturn(null);

    UsageInfo result = dao.getInfo(uuid, "t-missing");

    assertThat(result).isNull();
  }

  @Test
  void getRelatedReturnsEmptyList() {
    var uuid = UUID.randomUUID();

    List<SimpleUsage> result = dao.getRelated(uuid, "t-1", "some-type");

    assertThat(result).isEmpty();
  }

  private Taxon createTaxon(String id, TaxonomicStatus status) {
    var name = new Name();
    name.setScientificName("Test species");
    name.setRank(Rank.SPECIES);

    var taxon = new Taxon();
    taxon.setId(id);
    taxon.setDatasetKey(101);
    taxon.setName(name);
    taxon.setStatus(status);
    taxon.setOrigin(Origin.SOURCE);
    return taxon;
  }

  private Synonym createSynonym(String id) {
    var name = new Name();
    name.setScientificName("Old name");
    name.setRank(Rank.SPECIES);

    var synonym = new Synonym();
    synonym.setId(id);
    synonym.setDatasetKey(101);
    synonym.setName(name);
    synonym.setStatus(TaxonomicStatus.SYNONYM);
    synonym.setOrigin(Origin.SOURCE);
    return synonym;
  }
}
