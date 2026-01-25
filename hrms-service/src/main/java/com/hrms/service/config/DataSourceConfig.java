package com.hrms.service.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Value("${spring.datasource.url}")
    private String url;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Value("${spring.datasource.dbcp2.initial-size:5}")
    private int initialSize;
    
    @Value("${spring.datasource.dbcp2.max-total:20}")
    private int maxTotal;
    
    @Value("${spring.datasource.dbcp2.max-idle:10}")
    private int maxIdle;
    
    @Value("${spring.datasource.dbcp2.min-idle:5}")
    private int minIdle;
    
    @Value("${spring.datasource.dbcp2.max-wait-millis:30000}")
    private long maxWaitMillis;
    
    @Value("${spring.datasource.dbcp2.validation-query:SELECT 1}")
    private String validationQuery;
    
    @Value("${spring.datasource.dbcp2.test-on-borrow:true}")
    private boolean testOnBorrow;
    
    @Value("${spring.datasource.dbcp2.test-while-idle:true}")
    private boolean testWhileIdle;
    
    @Value("${spring.datasource.dbcp2.time-between-eviction-runs-millis:60000}")
    private long timeBetweenEvictionRunsMillis;

    @Bean
    @Primary
    public DataSource dataSource() {
        var ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setInitialSize(initialSize);
        ds.setMaxTotal(maxTotal);
        ds.setMaxIdle(maxIdle);
        ds.setMinIdle(minIdle);
        ds.setMaxWaitMillis(maxWaitMillis);
        ds.setValidationQuery(validationQuery);
        ds.setTestOnBorrow(testOnBorrow);
        ds.setTestWhileIdle(testWhileIdle);
        ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        return ds;
    }

}

