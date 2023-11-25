package com.stellariver.milky.demo.infrastructure.database;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.ReplacePlaceholderInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.stellariver.milky.common.tool.stable.MilkyStableSupport;
import com.stellariver.milky.infrastructure.base.database.BlockDeepPagingInnerInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author houchuang
 */

@SuppressWarnings("AliDeprecation")
@Configuration
@MapperScan(basePackages = "com.stellariver.milky.demo.infrastructure.database.mapper")
public class MyBatisPlusConfiguration {


    @Bean
    public SqlSessionFactory sqlSessionFactory(
            DataSource dataSource, @Autowired(required = false) MilkyStableSupport milkyStableSupport) throws Exception {

        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);

        sqlSessionFactoryBean.setTypeAliasesPackage("com.stellariver.milky.demo.infrastructure.database.entity");
        sqlSessionFactoryBean.setTypeHandlersPackage("com.stellariver.milky.demo.infrastructure.database.handler");

        //驼峰转化开启
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setReturnInstanceForEmptyRow(true);
        sqlSessionFactoryBean.setConfiguration(configuration);

        //mapper
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] mapperResources = resolver.getResources("mapper/*.xml");
        sqlSessionFactoryBean.setMapperLocations(mapperResources);

        // 自定义日期自动更新器
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);

        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        mybatisPlusInterceptor.addInnerInterceptor(new ReplacePlaceholderInnerInterceptor());
        mybatisPlusInterceptor.addInnerInterceptor(new BlockDeepPagingInnerInterceptor(true, 1000L));
        sqlSessionFactoryBean.setPlugins(mybatisPlusInterceptor);

        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory baseSqlSessionFactory) {
        return new SqlSessionTemplate(baseSqlSessionFactory);
    }

}
