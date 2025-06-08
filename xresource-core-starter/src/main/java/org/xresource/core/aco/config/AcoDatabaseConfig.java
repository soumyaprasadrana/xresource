package org.xresource.core.aco.config;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = { "xresource.aco.enabled" }, havingValue = "true")
@EnableConfigurationProperties(AcoDataSourceProperties.class)
@EnableTransactionManagement
@EntityScan(basePackages = "org.xresource.core.aco.entity")
@EnableJpaRepositories(basePackages = "org.xresource.core.aco.repository", entityManagerFactoryRef = "acoEntityManagerFactory", transactionManagerRef = "acoTransactionManager")
public class AcoDatabaseConfig {

    @Autowired
    private AcoDataSourceProperties acoProperties;

    @Bean
    public DataSource acoDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();

        if (acoProperties.getUrl() != null && !acoProperties.getUrl().isBlank()) {
            acoProperties.validate();
            ds.setDriverClassName(acoProperties.getDriverClassName());
            ds.setUrl(acoProperties.getUrl());
            ds.setUsername(acoProperties.getUsername());
            ds.setPassword(acoProperties.getPassword());
        } else {
            // Default to file-based H2 in SQLite compatibility mode
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:file:./aco-h2-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE");
            ds.setUsername("sa");
            ds.setPassword("");
        }

        return ds;
    }

    @Bean(name = "acoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean acoEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(acoDataSource());
        em.setPackagesToScan("org.xresource.core.aco.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");

        // Dynamically select dialect
        if (acoProperties.getUrl() != null && acoProperties.getUrl().contains("postgresql")) {
            jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        } else {
            jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        }

        em.setJpaPropertyMap(jpaProperties);
        return em;
    }

    @Bean(name = "acoTransactionManager")
    public PlatformTransactionManager acoTransactionManager(
            @Qualifier("acoEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(initMethod = "migrate")
    public Flyway flywayAco() {

        return Flyway.configure()
                .dataSource(acoDataSource())
                .locations("classpath:db/migration/aco")
                .baselineOnMigrate(true)
                .load();
    }
}
