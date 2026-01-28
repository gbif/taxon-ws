package org.gbif.species.config;

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
