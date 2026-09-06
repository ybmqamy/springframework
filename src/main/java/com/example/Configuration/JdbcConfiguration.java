package com.example.Configuration;

import com.example.Annotation.*;
import com.example.Bean.TransactionalProxyBeanPostProcessor;
import com.example.Jdbc.JdbcTemplate;
import com.example.Manager.DataSourceTransactionManager;
import com.example.Manager.platformTransactionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/// 配置datasource的数据源
/// Spring Boot有一个默认配置的数据源，并采用HikariCP作为连接池
@Configuration
@Component
public class JdbcConfiguration {
    @Bean
    DataSource datasource(
            @Value("${app.datasource.url}") String url,
            @Value("${app.datasource.username}") String username,
            @Value("${app.datasource.password}") String password,
            @Value("${app.datasource.driver-class-name:}") String driver,
            @Value("${app.datasource.maximum-pool-size:20}") int maximumPoolSize,
            @Value("${app.datasource.minimum-pool-size:1}") int minimumPoolSize,
            @Value("${app.datasource.connection-timeout:30000}") int connTimeout
    ){
        var config = new HikariConfig();
        config.setAutoCommit(false); ///这里需要手动提交
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if(driver!=null){
            config.setDriverClassName(driver);
        }
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumPoolSize);
        config.setConnectionTimeout(connTimeout);
        return new HikariDataSource(config);
    }

    @Bean
    TransactionalProxyBeanPostProcessor transactionalProxyBeanPostProcessor(){
        return new TransactionalProxyBeanPostProcessor();
    }
    @Bean
    public JdbcTemplate jdbcTemplate(@Autowired(value =true) DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
    @Bean
    public platformTransactionManager platformTransactionManager(@Autowired DataSource dataSource){
        return  new DataSourceTransactionManager(dataSource);
    }
}
