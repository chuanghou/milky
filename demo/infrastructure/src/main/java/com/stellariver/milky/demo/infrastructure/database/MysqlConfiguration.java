//package com.stellariver.milky.demo.infrastructure.database;
//
//import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
//import com.alibaba.druid.support.http.StatViewServlet;
//import com.alibaba.druid.support.http.WebStatFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//import javax.servlet.Filter;
//import javax.servlet.Servlet;
//import javax.sql.DataSource;
//
//@Configuration
//public class MysqlConfiguration {
//
//    @Bean
//    public DataSource dataSource() {
//        return DruidDataSourceBuilder.create().build();
//    }
//
//    /**
//     * 配置Druid 监控启动页面
//     * @return servletRegistrationBean
//     */
//    @Bean
//    public ServletRegistrationBean<Servlet> druidStartViewServlet() {
//        ServletRegistrationBean<Servlet> servletRegistrationBean = new ServletRegistrationBean<Servlet>(new StatViewServlet(), "/druid/*");
//        // 白名单
//        servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
//        // 黑名单
//        servletRegistrationBean.addInitParameter("deny", "192.168.1.100");
//        // 登录查看信息的账密，用于登录Druid监控后台
//        servletRegistrationBean.addInitParameter("loginUsername", "druid");
//        servletRegistrationBean.addInitParameter("loginPassword", "druid");
//        // 是否能够重置数据
//        servletRegistrationBean.addInitParameter("resetEnable", "true");
//        return servletRegistrationBean;
//    }
//
//    @Bean
//    public FilterRegistrationBean<Filter> filterRegistrationBean() {
//        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
//        filterFilterRegistrationBean.setFilter(new WebStatFilter());
//        // 添加过滤规则
//        filterFilterRegistrationBean.addUrlPatterns("/*");
//        // 添加不需要忽略的格式信息
//        filterFilterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
//        return filterFilterRegistrationBean;
//    }
//
//}