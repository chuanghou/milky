package com.stellariver.milky.demo.infrastructure.database;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import com.stellariver.milky.infrastructure.base.database.DeepPageFilter;
import com.stellariver.milky.infrastructure.base.database.SqlLogFilter;
import com.stellariver.milky.spring.partner.SectionLoader;
import com.stellariver.milky.spring.partner.UniqueIdBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author houchuang
 */
@Configuration
public class DruidConfiguration {

    @Bean
    public SqlLogFilter sqlLogFilter() {
        return new SqlLogFilter();
    }

    @Bean
    public MyDeepPageFilter deepPageFilter() {
        return new MyDeepPageFilter(false, 100000);
    }


    @Getter
    static public class MyDeepPageFilter extends DeepPageFilter {

        private Integer triggerTimes = 0;

        public MyDeepPageFilter(boolean block, int limit) {
            super(block, limit);
        }

        @Override
        protected void customStrategyWhenFail(String sql) {
            triggerTimes++;
        }

    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.a")
    public DataSource dataSourceA(List<Filter> filters) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.getProxyFilters().addAll(filters);
        return druidDataSource;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.b")
    public DataSource dataSourceB(List<Filter> filters) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.getProxyFilters().addAll(filters);
        return druidDataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("dataSourceA") DataSource dataSourceA, @Qualifier("dataSourceB") DataSource dataSourceB) {
        return new RoutingDataSource(dataSourceA, dataSourceB);
    }

    static public class RoutingDataSource extends AbstractRoutingDataSource {

        public RoutingDataSource(DataSource dataSourceA, DataSource dataSourceB)  {
            setTargetDataSources(Collect.asMap("dataSourceA", dataSourceA, "dataSourceB", dataSourceB));
        }

        @Override
        protected Object determineCurrentLookupKey() {
            return "dataSourceA";
        }

    }

    @Bean
    public SectionLoader sectionLoader(DataSource dataSource) {
        return new SectionLoader(dataSource);
    }

    @Bean
    public UniqueIdBuilder uniqueIdBuilder(SectionLoader sectionLoader) {
        return new UniqueIdBuilder("unique_id", "test", sectionLoader);
    }

    @Bean
    public UniqueIdGetter uniqueIdGetter(UniqueIdBuilder uniqueIdBuilder) {
        return new UniqueIdGetterImpl(uniqueIdBuilder);
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class UniqueIdGetterImpl implements UniqueIdGetter{

        final UniqueIdBuilder uniqueIdBuilder;

        @Override
        public Long get() {
            return uniqueIdBuilder.get();
        }

    }

}
