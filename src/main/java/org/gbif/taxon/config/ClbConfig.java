package org.gbif.taxon.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import life.catalogue.cache.LatestDatasetKeyCache;

import life.catalogue.cache.LatestDatasetKeyCacheImpl;

import life.catalogue.config.EsConfig;
import life.catalogue.config.IndexConfig;
import life.catalogue.es.EsClientFactory;
import life.catalogue.es.search.NameUsageSearchService;
import life.catalogue.es.search.NameUsageSearchServiceEs;
import life.catalogue.es.suggest.NameUsageSuggestionService;

import life.catalogue.es.suggest.NameUsageSuggestionServiceEs;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

import java.util.UUID;

@Configuration
public class ClbConfig {
  private static final Logger LOG = LoggerFactory.getLogger(ClbConfig.class);

  @Bean
  @Primary
  public LatestDatasetKeyCache latestDatasetKeyCache(SqlSessionFactory factory) {
    return new LatestDatasetKeyCacheImpl(factory);
  }

  @Bean
  public DatasetKeys datasetKeys(
    @Value("${datasets.iucn:19491596-35ae-4a91-9a98-85cf505f1bd3}") String iucn,
    @Value("${datasets.citesI:2104}") Integer cites1,
    @Value("${datasets.citesII:2094}") Integer cites2
  ) {
    var d = new DatasetKeys();
    d.setIucn( iucn == null ? null : UUID.fromString(iucn) );
    d.setCitesI(cites1);
    d.setCitesII(cites2);
    return d;
  }

  @Bean
  public EsConfig esClientConfiguration(
    @Value("${elasticsearch.index.name}") String name,
    @Value("${elasticsearch.hosts:localhost:9200}") String hosts,
    @Value("${elasticsearch.connectTimeout:10000}") int connectTimeout,
    @Value("${elasticsearch.socketTimeout:900000}") int socketTimeout,
    @Value("${elasticsearch.maxConnPerRoute:20}") int maxConnPerRoute,
    @Value("${elasticsearch.maxConnTotal:100}") int maxConnTotal
  ) {
    var cfg = new EsConfig();
    cfg.hosts = hosts;
    cfg.index = new IndexConfig();
    cfg.index.name = name;
    cfg.connectTimeout = connectTimeout;
    cfg.socketTimeout = socketTimeout;
    cfg.maxConnPerRoute = maxConnPerRoute;
    cfg.maxConnTotal = maxConnTotal;
    return cfg;
  }

  @Bean
  @Primary
  public ElasticsearchClient esClient(EsConfig cfg) {
    return new EsClientFactory(cfg).createClient();
  }

  @Bean
  @Primary
  public NameUsageSearchService nameUsageSearchService(EsConfig cfg, ElasticsearchClient client) {
    return new NameUsageSearchServiceEs(resolveIndexName(cfg), client);
  }

  @Bean
  @Primary
  public NameUsageSuggestionService nameUsageSuggestionService(EsConfig cfg, ElasticsearchClient client) {
    return new NameUsageSuggestionServiceEs(resolveIndexName(cfg), client);
  }

  private static String resolveIndexName(EsConfig cfg) {
    if (cfg == null) {
      throw new IllegalStateException("Missing Elasticsearch configuration bean (EsConfig).");
    }
    if (cfg.index == null || cfg.index.name == null || cfg.index.name.isBlank()) {
      throw new IllegalStateException(
        "Missing Elasticsearch index configuration. Please configure 'elasticsearch.index.name' (and related settings)"
      );
    }
    return cfg.index.name;
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
