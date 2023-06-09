package com.stellariver.milky.demo.infrastructure.database;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import com.stellariver.milky.spring.partner.UniqueIdBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author houchuang
 */
@Configuration
public class DruidConfiguration {

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
    public UniqueIdGetter uniqueIdGetter(DataSource dataSource) {
        UniqueIdBuilder uniqueIdBuilder = new UniqueIdBuilder(dataSource, "unique_id", "test");
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
