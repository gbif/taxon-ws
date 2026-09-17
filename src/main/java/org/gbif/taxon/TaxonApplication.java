
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.taxon;

import com.zaxxer.hikari.HikariDataSource;
import life.catalogue.dao.DatasetInfoCache;
import life.catalogue.db.MybatisFactory;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticsearchRestHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {ElasticsearchRestHealthContributorAutoConfiguration.class})
@EnableConfigurationProperties
@ComponentScan(basePackages = {"org.gbif.ws.server.mapper", "org.gbif.taxon", "org.gbif.taxon.resource"})
@EnableScheduling
public class TaxonApplication {

  @Bean
  public SqlSessionFactory factory(HikariDataSource dataSource, ObjectProvider<Interceptor> interceptors) {
    var factory = MybatisFactory.configure(dataSource, "test-env");
    // set factory in DatasetInfoCache singleton
    DatasetInfoCache.CACHE.setFactory(factory);
    // register any MyBatis interceptor beans (e.g. SlowSqlInterceptor when monitoring.slow-sql.enabled=true)
    interceptors.orderedStream().forEach(factory.getConfiguration()::addInterceptor);
    return factory;
  }

  public static void main(String[] args) {
    SpringApplication.run(TaxonApplication.class, args);
  }

}
