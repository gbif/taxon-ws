package org.gbif.taxon.dao;

import life.catalogue.api.model.CslData;
import life.catalogue.api.model.Name;
import life.catalogue.api.model.SimpleName;
import life.catalogue.api.model.Synonym;
import life.catalogue.api.model.Synonymy;
import life.catalogue.api.model.Taxon;
import life.catalogue.api.model.TaxonProperty;
import life.catalogue.api.model.TreeNode;
import life.catalogue.api.vocab.Country;
import life.catalogue.api.vocab.DegreeOfEstablishment;
import life.catalogue.api.vocab.Environment;
import life.catalogue.api.vocab.EstablishmentMeans;
import life.catalogue.api.vocab.License;
import life.catalogue.api.vocab.MediaType;
import life.catalogue.api.vocab.NomStatus;
import life.catalogue.api.vocab.Origin;
import life.catalogue.api.vocab.Sex;
import life.catalogue.api.vocab.ThreatStatus;

import org.gbif.nameparser.api.NomCode;
import org.gbif.nameparser.api.Rank;
import org.gbif.taxon.api.Distribution;
import org.gbif.taxon.api.Media;
import org.gbif.taxon.api.NameUsage;
import org.gbif.taxon.api.Reference;
import org.gbif.taxon.api.SimpleUsage;
import org.gbif.taxon.api.TreeUsage;
import org.gbif.taxon.api.UsageInfo;
import org.gbif.taxon.api.VernacularName;

import java.net.URI;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import life.catalogue.api.vocab.TaxonomicStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiConverterTest {

  @Mock
  private DatasetKeyMap map;

  private ApiConverter converter;

  @BeforeEach
  void setUp() {
    converter = new ApiConverter(map);
  }

  // --- convert(NameUsageBase) tests ---

  @Test
  void convertAcceptedTaxon() {
    var uuid = UUID.randomUUID();
    var name = new Name();
    name.setId("name-1");
    name.setScientificName("Abies alba");
    name.setAuthorship("Mill.");
    name.setRank(Rank.SPECIES);
    name.setCode(NomCode.BOTANICAL);
    name.setNomStatus(NomStatus.ESTABLISHED);
    name.setPublishedInId("pub-1");
    name.setType(org.gbif.nameparser.api.NameType.SCIENTIFIC);
    name.setGenus("Abies");
    name.setSpecificEpithet("alba");

    var taxon = new Taxon();
    taxon.setId("t-1");
    taxon.setDatasetKey(101);
    taxon.setName(name);
    taxon.setStatus(TaxonomicStatus.ACCEPTED);
    taxon.setOrigin(Origin.SOURCE);
    taxon.setParentId("p-1");
    taxon.setExtinct(true);
    taxon.setAccordingToId("ref-1");
    taxon.setAccordingTo("Author 2020");
    taxon.setNamePhrase("sensu lato");
    taxon.setLink(URI.create("https://example.com/ref"));
    taxon.setRemarks("some remarks");

    when(map.toGBIF(101)).thenReturn(uuid);

    NameUsage nu = converter.convert(taxon);

    assertThat(nu.getTaxonID()).isEqualTo("t-1");
    assertThat(nu.getParentNameUsageID()).isEqualTo("p-1");
    assertThat(nu.getAcceptedNameUsageID()).isNull();
    assertThat(nu.getScientificName()).isEqualTo("Abies alba");
    assertThat(nu.getScientificNameAuthorship()).isEqualTo("Mill.");
    assertThat(nu.getTaxonRank()).isEqualTo(Rank.SPECIES);
    assertThat(nu.getTaxonomicStatus()).isEqualTo(TaxonomicStatus.ACCEPTED);
    assertThat(nu.getNomenclaturalCode()).isEqualTo("BOTANICAL");
    assertThat(nu.getExtinct()).isTrue();
    assertThat(nu.getDatasetKey()).isEqualTo(uuid);
    assertThat(nu.getScientificNameID()).isEqualTo("name-1");
    assertThat(nu.getAcceptedNameUsage()).isNull();
    assertThat(nu.getNameAccordingTo()).isEqualTo("Author 2020");
    assertThat(nu.getNamePhrase()).isEqualTo("sensu lato");
    assertThat(nu.getNomenclaturalStatus()).isEqualTo("ESTABLISHED");
    assertThat(nu.getNameType()).isEqualTo(org.gbif.nameparser.api.NameType.SCIENTIFIC);
    assertThat(nu.getGenericName()).isEqualTo("Abies");
    assertThat(nu.getSpecificEpithet()).isEqualTo("alba");
    assertThat(nu.getReferences()).isEqualTo(URI.create("https://example.com/ref"));
    assertThat(nu.getTaxonRemarks()).isEqualTo("some remarks");
  }

  @Test
  void convertSynonym() {
    var uuid = UUID.randomUUID();
    var name = new Name();
    name.setId("name-2");
    name.setScientificName("Pinus picea");
    name.setAuthorship("L.");
    name.setRank(Rank.SPECIES);

    var acceptedTaxon = new Taxon();
    acceptedTaxon.setId("t-1");
    acceptedTaxon.setDatasetKey(101);
    acceptedTaxon.setStatus(TaxonomicStatus.ACCEPTED);
    acceptedTaxon.setOrigin(Origin.SOURCE);
    var acceptedName = new Name();
    acceptedName.setScientificName("Abies alba");
    acceptedName.setRank(Rank.SPECIES);
    acceptedTaxon.setName(acceptedName);

    var synonym = new Synonym();
    synonym.setId("s-1");
    synonym.setDatasetKey(101);
    synonym.setName(name);
    synonym.setStatus(TaxonomicStatus.SYNONYM);
    synonym.setOrigin(Origin.SOURCE);
    synonym.setParentId("t-1");
    synonym.setAccepted(acceptedTaxon);

    when(map.toGBIF(101)).thenReturn(uuid);

    NameUsage nu = converter.convert(synonym);

    assertThat(nu.getTaxonID()).isEqualTo("s-1");
    assertThat(nu.getAcceptedNameUsageID()).isEqualTo("t-1");
    assertThat(nu.getParentNameUsageID()).isNull();
    assertThat(nu.getAcceptedNameUsage()).isNotNull();
    assertThat(nu.getExtinct()).isNull();
  }

  @Test
  void convertWithNullCodeAndStatus() {
    var uuid = UUID.randomUUID();
    var name = new Name();
    name.setId("name-3");
    name.setScientificName("Test species");
    name.setRank(Rank.SPECIES);
    // code and nomStatus are null

    var taxon = new Taxon();
    taxon.setId("t-2");
    taxon.setDatasetKey(101);
    taxon.setName(name);
    taxon.setStatus(TaxonomicStatus.ACCEPTED);
    taxon.setOrigin(Origin.SOURCE);

    when(map.toGBIF(101)).thenReturn(uuid);

    NameUsage nu = converter.convert(taxon);

    assertThat(nu.getNomenclaturalCode()).isNull();
    assertThat(nu.getNomenclaturalStatus()).isNull();
  }

  // --- convertSimple(SimpleName) tests ---

  @Test
  void convertSimpleAccepted() {
    var sn = new SimpleName("id-1", "Homo sapiens", "Linnaeus, 1758", Rank.SPECIES);
    sn.setStatus(TaxonomicStatus.ACCEPTED);
    sn.setParent("parent-1");
    sn.setCode(NomCode.ZOOLOGICAL);
    sn.setExtinct(false);

    SimpleUsage su = converter.convert(sn);

    assertThat(su.getTaxonID()).isEqualTo("id-1");
    assertThat(su.getParentNameUsageID()).isEqualTo("parent-1");
    assertThat(su.getAcceptedNameUsageID()).isNull();
    assertThat(su.getScientificName()).isEqualTo("Homo sapiens");
    assertThat(su.getScientificNameAuthorship()).isEqualTo("Linnaeus, 1758");
    assertThat(su.getTaxonRank()).isEqualTo(Rank.SPECIES);
    assertThat(su.getTaxonomicStatus()).isEqualTo(TaxonomicStatus.ACCEPTED);
    assertThat(su.getNomenclaturalCode()).isEqualTo("ZOOLOGICAL");
    assertThat(su.getExtinct()).isFalse();
  }

  @Test
  void convertSimpleSynonym() {
    var sn = new SimpleName("id-2", "Old name", Rank.SPECIES);
    sn.setStatus(TaxonomicStatus.SYNONYM);
    sn.setParent("accepted-1");

    SimpleUsage su = converter.convert(sn);

    assertThat(su.getAcceptedNameUsageID()).isEqualTo("accepted-1");
    assertThat(su.getParentNameUsageID()).isNull();
  }

  @Test
  void convertSimpleNullCode() {
    var sn = new SimpleName("id-3", "Some name", Rank.GENUS);
    sn.setStatus(TaxonomicStatus.ACCEPTED);

    SimpleUsage su = converter.convert(sn);

    assertThat(su.getNomenclaturalCode()).isNull();
  }

  // --- convertTree(TreeNode) tests ---

  @Test
  void convertTreeWithStatus() {
    var tn = mock(TreeNode.class);
    when(tn.getId()).thenReturn("tn-1");
    when(tn.getStatus()).thenReturn(TaxonomicStatus.ACCEPTED);
    when(tn.getParentId()).thenReturn("parent-1");
    when(tn.getName()).thenReturn("Plantae");
    when(tn.getAuthorship()).thenReturn(null);
    when(tn.getRank()).thenReturn(Rank.KINGDOM);
    when(tn.getLabelHtml()).thenReturn("<i>Plantae</i>");
    when(tn.getChildCount()).thenReturn(42);
    when(tn.getCount()).thenReturn(350000);

    TreeUsage tu = converter.convertTree(tn);

    assertThat(tu.getTaxonID()).isEqualTo("tn-1");
    assertThat(tu.getParentNameUsageID()).isEqualTo("parent-1");
    assertThat(tu.getAcceptedNameUsageID()).isNull();
    assertThat(tu.getScientificName()).isEqualTo("Plantae");
    assertThat(tu.getTaxonRank()).isEqualTo(Rank.KINGDOM);
    assertThat(tu.getTaxonomicStatus()).isEqualTo(TaxonomicStatus.ACCEPTED);
    assertThat(tu.getLabel()).isEqualTo("<i>Plantae</i>");
    assertThat(tu.getChildren()).isEqualTo(42);
    assertThat(tu.getSpecies()).isEqualTo(350000);
  }

  @Test
  void convertTreeWithNullStatus() {
    var tn = mock(TreeNode.class);
    when(tn.getId()).thenReturn("tn-2");
    when(tn.getStatus()).thenReturn(null);
    when(tn.getParentId()).thenReturn("parent-2");
    when(tn.getChildCount()).thenReturn(0);
    when(tn.getCount()).thenReturn(null);

    TreeUsage tu = converter.convertTree(tn);

    assertThat(tu.getParentNameUsageID()).isEqualTo("parent-2");
    assertThat(tu.getAcceptedNameUsageID()).isNull();
    assertThat(tu.getSpecies()).isEqualTo(0);
  }

  @Test
  void convertTreeSynonym() {
    var tn = mock(TreeNode.class);
    when(tn.getId()).thenReturn("tn-3");
    when(tn.getStatus()).thenReturn(TaxonomicStatus.SYNONYM);
    when(tn.getParentId()).thenReturn("accepted-1");
    when(tn.getChildCount()).thenReturn(0);
    when(tn.getCount()).thenReturn(null);

    TreeUsage tu = converter.convertTree(tn);

    assertThat(tu.getAcceptedNameUsageID()).isEqualTo("accepted-1");
    assertThat(tu.getParentNameUsageID()).isNull();
  }

  // --- convert(Media) tests ---

  @Test
  void convertMediaFullFields() {
    var m = new life.catalogue.api.model.Media();
    m.setUrl(URI.create("https://example.com/image.jpg"));
    m.setType(MediaType.IMAGE);
    m.setTitle("A nice photo");
    m.setCaptured(LocalDate.of(2023, 6, 15));
    m.setCapturedBy("John Smith");
    m.setLicense(License.CC_BY);
    m.setLink(URI.create("https://example.com/details"));
    m.setRemarks("Taken in the wild");

    Media media = converter.convert(m);

    assertThat(media.getIdentifier()).isEqualTo("https://example.com/image.jpg");
    assertThat(media.getType()).isEqualTo("IMAGE");
    assertThat(media.getTitle()).isEqualTo("A nice photo");
    assertThat(media.getCreated()).isEqualTo("2023-06-15");
    assertThat(media.getCreator()).isEqualTo("John Smith");
    assertThat(media.getLicense()).isEqualTo("CC_BY");
    assertThat(media.getReferences()).isEqualTo(URI.create("https://example.com/details"));
    assertThat(media.getRemarks()).isEqualTo("Taken in the wild");
  }

  @Test
  void convertMediaNullFields() {
    var m = new life.catalogue.api.model.Media();
    m.setTitle("Title only");

    Media media = converter.convert(m);

    assertThat(media.getIdentifier()).isNull();
    assertThat(media.getType()).isNull();
    assertThat(media.getCreated()).isNull();
    assertThat(media.getLicense()).isNull();
    assertThat(media.getTitle()).isEqualTo("Title only");
  }

  // --- convert(VernacularName) tests ---

  @Test
  void convertVernacularNameFullFields() {
    var vn = new life.catalogue.api.model.VernacularName();
    vn.setName("Silver Fir");
    vn.setLanguage("eng");
    vn.setArea("Central Europe");
    vn.setCountry(Country.GERMANY);
    vn.setSex(Sex.MALE);
    vn.setPreferred(true);
    vn.setRemarks("Common name");

    VernacularName result = converter.convert(vn);

    assertThat(result.getVernacularName()).isEqualTo("Silver Fir");
    assertThat(result.getLanguage()).isEqualTo("eng");
    assertThat(result.getLocality()).isEqualTo("Central Europe");
    assertThat(result.getCountryCode()).isEqualTo("DE");
    assertThat(result.getSex()).isEqualTo("MALE");
    assertThat(result.getPreferredName()).isTrue();
    assertThat(result.getRemarks()).isEqualTo("Common name");
  }

  @Test
  void convertVernacularNameNullCountryAndSex() {
    var vn = new life.catalogue.api.model.VernacularName();
    vn.setName("Common name");

    VernacularName result = converter.convert(vn);

    assertThat(result.getVernacularName()).isEqualTo("Common name");
    assertThat(result.getCountryCode()).isNull();
    assertThat(result.getSex()).isNull();
  }

  // --- convert(Distribution) tests ---

  @Test
  void convertDistributionWithCountryArea() {
    var d = new life.catalogue.api.model.Distribution();
    d.setArea(Country.CANADA);
    d.setLifeStage("adult");
    d.setEstablishmentMeans(EstablishmentMeans.NATIVE);
    d.setDegreeOfEstablishment(DegreeOfEstablishment.ESTABLISHED);
    d.setPathway("natural");
    d.setThreatStatus(ThreatStatus.LEAST_CONCERN);
    d.setYear(2020);
    d.setRemarks("Common");

    Distribution dist = converter.convert(d);

    assertThat(dist.getLocality()).isEqualTo(Country.CANADA.getName());
    assertThat(dist.getCountryCode()).isEqualTo("CA");
    assertThat(dist.getLifeStage()).isEqualTo("adult");
    assertThat(dist.getEstablishmentMeans()).isEqualTo("NATIVE");
    assertThat(dist.getDegreeOfEstablishment()).isEqualTo("ESTABLISHED");
    assertThat(dist.getPathway()).isEqualTo("natural");
    assertThat(dist.getThreatStatus()).isEqualTo("LEAST_CONCERN");
    assertThat(dist.getEventDate()).isEqualTo("2020");
    assertThat(dist.getRemarks()).isEqualTo("Common");
  }

  @Test
  void convertDistributionNonCountryArea() {
    var area = mock(life.catalogue.api.vocab.Area.class);
    when(area.getName()).thenReturn("Some Region");

    var d = new life.catalogue.api.model.Distribution();
    d.setArea(area);

    Distribution dist = converter.convert(d);

    assertThat(dist.getLocality()).isEqualTo("Some Region");
    assertThat(dist.getCountryCode()).isNull();
  }

  @Test
  void convertDistributionNullArea() {
    var d = new life.catalogue.api.model.Distribution();

    Distribution dist = converter.convert(d);

    assertThat(dist.getLocality()).isNull();
    assertThat(dist.getCountryCode()).isNull();
  }

  @Test
  void convertDistributionNullEnums() {
    var d = new life.catalogue.api.model.Distribution();
    d.setArea(Country.BRAZIL);

    Distribution dist = converter.convert(d);

    assertThat(dist.getEstablishmentMeans()).isNull();
    assertThat(dist.getDegreeOfEstablishment()).isNull();
    assertThat(dist.getThreatStatus()).isNull();
    assertThat(dist.getEventDate()).isNull();
  }

  // --- convert(Reference) tests ---

  @Test
  void convertReferenceWithCslDoi() {
    var csl = new CslData();
    csl.setDOI("10.1234/example");

    var r = new life.catalogue.api.model.Reference();
    r.setId("ref-1");
    r.setDatasetKey(101);
    r.setCitation("Smith et al. 2020");
    r.setCsl(csl);
    r.setRemarks("Important paper");

    Reference ref = converter.convert(r);

    assertThat(ref.getReferenceID()).isEqualTo("ref-1");
    assertThat(ref.getDoi()).isEqualTo("10.1234/example");
    assertThat(ref.getCitation()).isEqualTo("Smith et al. 2020");
    assertThat(ref.getRemarks()).isEqualTo("Important paper");
  }

  @Test
  void convertReferenceWithoutCsl() {
    var r = new life.catalogue.api.model.Reference();
    r.setId("ref-2");
    r.setDatasetKey(101);
    r.setCitation("Jones 2019");

    Reference ref = converter.convert(r);

    assertThat(ref.getReferenceID()).isEqualTo("ref-2");
    assertThat(ref.getDoi()).isNull();
    assertThat(ref.getCitation()).isEqualTo("Jones 2019");
  }

  // --- convert(UsageInfo) tests ---

  @Test
  void convertUsageInfoComposite() {
    var name = new Name();
    name.setId("name-1");
    name.setScientificName("Abies alba");
    name.setRank(Rank.SPECIES);
    name.setCode(NomCode.BOTANICAL);

    var taxon = new Taxon();
    taxon.setId("t-1");
    taxon.setDatasetKey(101);
    taxon.setName(name);
    taxon.setStatus(TaxonomicStatus.ACCEPTED);
    taxon.setOrigin(Origin.SOURCE);
    taxon.setEnvironments(EnumSet.of(Environment.TERRESTRIAL, Environment.FRESHWATER));

    var ui = new life.catalogue.api.model.UsageInfo(taxon);

    // vernacular names
    var vn = new life.catalogue.api.model.VernacularName();
    vn.setName("Silver Fir");
    vn.setLanguage("eng");
    ui.setVernacularNames(List.of(vn));

    // media
    var m = new life.catalogue.api.model.Media();
    m.setTitle("Photo");
    ui.setMedia(List.of(m));

    // distributions
    var d = new life.catalogue.api.model.Distribution();
    d.setArea(Country.GERMANY);
    ui.setDistributions(List.of(d));

    // synonyms
    var synName = new Name();
    synName.setScientificName("Pinus picea");
    synName.setRank(Rank.SPECIES);
    var syn = new Synonym();
    syn.setId("syn-1");
    syn.setDatasetKey(101);
    syn.setName(synName);
    syn.setStatus(TaxonomicStatus.SYNONYM);
    syn.setOrigin(Origin.SOURCE);
    var synonymy = new Synonymy();
    synonymy.getHeterotypic().add(syn);
    ui.setSynonyms(synonymy);

    // references
    var ref = new life.catalogue.api.model.Reference();
    ref.setId("ref-1");
    ref.setDatasetKey(101);
    ref.setCitation("Author 2020");
    ui.addReference(ref);

    // properties
    var iucnProp = new TaxonProperty();
    iucnProp.setProperty("iucnRedlistStatus");
    iucnProp.setValue("LC");
    var citesProp = new TaxonProperty();
    citesProp.setProperty("citesAppendix");
    citesProp.setValue("II");
    ui.setProperties(List.of(iucnProp, citesProp));

    UsageInfo info = converter.convert(ui);

    assertThat(info.getTaxon().getTaxonID()).isEqualTo("t-1");
    assertThat(info.getVernacularNames()).hasSize(1);
    assertThat(info.getVernacularNames().get(0).getVernacularName()).isEqualTo("Silver Fir");
    assertThat(info.getMedia()).hasSize(1);
    assertThat(info.getDistributions()).hasSize(1);
    assertThat(info.getSynonyms()).hasSize(1);
    assertThat(info.getBibliography()).hasSize(1);
    assertThat(info.getChecklistBankLink()).isNotNull();
    assertThat(info.getChecklistBankLink().toString()).contains("dataset/101/taxon/t-1");
    assertThat(info.getEnvironment()).containsExactlyInAnyOrder("TERRESTRIAL", "FRESHWATER");
    assertThat(info.getIucnRedlistStatus()).isEqualTo("LC");
    assertThat(info.getCitesAppendix()).isEqualTo("II");
  }

  @Test
  void convertUsageInfoNullCollections() {
    var name = new Name();
    name.setId("name-1");
    name.setScientificName("Test species");
    name.setRank(Rank.SPECIES);

    var taxon = new Taxon();
    taxon.setId("t-2");
    taxon.setDatasetKey(101);
    taxon.setName(name);
    taxon.setStatus(TaxonomicStatus.ACCEPTED);
    taxon.setOrigin(Origin.SOURCE);

    var ui = new life.catalogue.api.model.UsageInfo(taxon);
    // no vernacular names, synonyms, media, distributions, or properties set

    UsageInfo info = converter.convert(ui);

    assertThat(info.getTaxon().getTaxonID()).isEqualTo("t-2");
    assertThat(info.getIucnRedlistStatus()).isNull();
    assertThat(info.getCitesAppendix()).isNull();
    assertThat(info.getChecklistBankLink()).isNotNull();
  }

}
