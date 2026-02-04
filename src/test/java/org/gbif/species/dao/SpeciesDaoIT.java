package org.gbif.species.dao;

import com.zaxxer.hikari.HikariDataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.gbif.species.api.SimpleUsage;
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
class SpeciesDaoIT {

  @MockitoBean
  private HikariDataSource dataSource;

  @MockitoBean
  private SqlSessionFactory sqlSessionFactory;

  @Autowired
  private SpeciesDao speciesDao;

  @Test
  void speciesDaoIsWired() {
    assertThat(speciesDao).isNotNull();
  }

  @Test
  void getRelatedReturnsEmptyList() {
    var uuid = UUID.randomUUID();
    List<SimpleUsage> result = speciesDao.getRelated(uuid, "t-1", "any-type");
    assertThat(result).isEmpty();
  }
}
