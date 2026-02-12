package org.gbif.taxon.dao;

import com.zaxxer.hikari.HikariDataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.taxon.api.SimpleUsage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaxonDaoIT {

  @MockitoBean
  private HikariDataSource dataSource;

  @MockitoBean
  private SqlSessionFactory sqlSessionFactory;

  @Autowired
  private TaxonDao taxonDao;

  @Test
  void taxonDaoIsWired() {
    assertThat(taxonDao).isNotNull();
  }

  @Test
  void getRelatedReturnsEmptyList() {
    var uuid = UUID.randomUUID();
    List<SimpleUsage> result = taxonDao.getRelated(uuid, "t-1", "any-type");
    assertThat(result).isEmpty();
  }
}
