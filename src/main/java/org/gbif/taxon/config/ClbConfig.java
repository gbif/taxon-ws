package org.gbif.taxon.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import life.catalogue.cache.LatestDatasetKeyCache;

import life.catalogue.cache.LatestDatasetKeyCacheImpl;

import life.catalogue.config.EsConfig;
import life.catalogue.es.EsClientFactory;
import life.catalogue.es.search.NameUsageSearchService;
import life.catalogue.es.search.NameUsageSearchServiceEs;
import life.catalogue.es.suggest.NameUsageSuggestionService;

import life.catalogue.es.suggest.NameUsageSuggestionServiceEs;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class ClbConfig {
  private static final Logger LOG = LoggerFactory.getLogger(ClbConfig.class);

  @Bean
  @Primary
  public LatestDatasetKeyCache latestDatasetKeyCache(SqlSessionFactory factory) {
    return new LatestDatasetKeyCacheImpl(factory);
  }

  @Bean
  @ConfigurationProperties(prefix = "elasticsearch")
  public EsConfig esClientConfiguration() {
    return new EsConfig();
  }

  @Bean
  @Primary
  public ElasticsearchClient esClient(EsConfig cfg) {
    return new EsClientFactory(cfg).createClient();
  }

  @Bean
  @Primary
  public NameUsageSearchService nameUsageSearchService(EsConfig cfg, ElasticsearchClient client) {
    return new NameUsageSearchServiceEs(cfg.index.name, client);
  }

  @Bean
  @Primary
  public NameUsageSuggestionService nameUsageSuggestionService(EsConfig cfg, ElasticsearchClient client) {
    return new NameUsageSuggestionServiceEs(cfg.index.name, client);
  }

  @Bean
  @Primary
  @ConfigurationProperties("datasource")
  public DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  @ConfigurationProperties("datasource.hikari")
  public HikariDataSource dataSource() {
    var props = dataSourceProperties();
    LOG.info("Connecting to {}", props.determineUrl());
    return props
      .initializeDataSourceBuilder()
      .type(HikariDataSource.class)
      .build();
  }
}
