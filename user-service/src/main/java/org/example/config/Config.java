package org.example.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
//@EnableWebMvc
@PropertySource("classpath:application.properties")
public class Config {

    @Value("${datasource.user}")
    private String dataSourceUser;
    @Value("${datasource.password}")
    private String dataSourcePassword;
    @Value("${jdbc.url}")
    private String jdbcUrl;
    @Value("${driver.class.name}")
    private String driverClassName;
    @Value("${schema_name}")
    private String schema_name;

    @Bean
    public DataSource dataSource() {

        Properties props = new Properties();

        props.setProperty("dataSource.user", dataSourceUser);
        props.setProperty("dataSource.password", dataSourcePassword);
        props.setProperty("jdbcUrl", jdbcUrl);
        HikariConfig config = new HikariConfig(props);
        config.setDriverClassName(driverClassName);
        config.setSchema(schema_name);
        return new HikariDataSource(config);

    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);


        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("org.example.dao.entities");
        factory.setPersistenceUnitName("user_service");
        factory.setDataSource(dataSource());
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {

        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }


}
