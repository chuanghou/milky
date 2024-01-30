package com.stellariver.milky.demo.infrastructure.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import com.stellariver.milky.infrastructure.base.database.MilkyLogFilter;
import com.stellariver.milky.spring.partner.SectionLoader;
import com.stellariver.milky.spring.partner.UniqueIdBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

import java.sql.SQLException;

/**
 * @author houchuang
 */
@Configuration
public class DruidConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.a")
    public DataSource dataSourceA() throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.addFilters(MilkyLogFilter.class.getName());
        return druidDataSource;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.b")
    public DataSource dataSourceB() throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.addFilters(MilkyLogFilter.class.getName());
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
    public ServletRegistrationBean<StatViewServlet> servletRegistrationBean() {
        ServletRegistrationBean<StatViewServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new StatViewServlet(), "/druid/*"
        );
        servletRegistrationBean.addInitParameter(StatViewServlet.PARAM_NAME_ALLOW, "127.0.0.1");
        servletRegistrationBean.addInitParameter(StatViewServlet.PARAM_NAME_DENY,"");
        servletRegistrationBean.addInitParameter(StatViewServlet.PARAM_NAME_RESET_ENABLE, "true");
        servletRegistrationBean.addInitParameter(StatViewServlet.PARAM_NAME_USERNAME, "hc");
        servletRegistrationBean.addInitParameter(StatViewServlet.PARAM_NAME_PASSWORD, "hc");
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<WebStatFilter> filterFilterRegistrationBean(WebStatFilter webStatFilter) {
        FilterRegistrationBean<WebStatFilter> webStatFilterFilterRegistrationBean = new FilterRegistrationBean<>(webStatFilter);
        webStatFilterFilterRegistrationBean.addUrlPatterns("/item/*");
        return webStatFilterFilterRegistrationBean;
    }

    @Bean
    public WebStatFilter webStatFilter() {
        WebStatFilter webStatFilter = new WebStatFilter();
        webStatFilter.setSessionStatEnable(true);
        return webStatFilter;
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
