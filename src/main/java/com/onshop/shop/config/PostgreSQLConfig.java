//package com.onshop.shop.config;
//
//import jakarta.persistence.EntityManagerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//    basePackages = "com.onshop.shop.vector",  // PostgreSQL 벡터 데이터 관련 Repository 경로
//    entityManagerFactoryRef = "pgEntityManagerFactory",
//    transactionManagerRef = "pgTransactionManager"
//)
//public class PostgreSQLConfig {
//
//    @Bean(name = "pgDataSource")
//    public DataSource pgDataSource() {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setUrl("jdbc:postgresql://localhost:5432/final");
//        dataSource.setUsername("postgres");
//        dataSource.setPassword("1234");
//        return dataSource;
//    }
//
//    @Bean(name = "pgEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean pgEntityManagerFactory(
//            EntityManagerFactoryBuilder builder,
//            @Qualifier("pgDataSource") DataSource dataSource) {
//
//        Map<String, Object> properties = new HashMap<>();
//        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//        properties.put("hibernate.hbm2ddl.auto", "update");  // 필요에 따라 변경 가능
//        properties.put("hibernate.show_sql", true);
//        properties.put("hibernate.format_sql", true);
//
//        return builder
//                .dataSource(dataSource)
//                .packages("com.onshop.shop.vector")  // @Entity 클래스들이 위치한 패키지
//                .persistenceUnit("pgPersistenceUnit")
//                .properties(properties)
//                .build();
//    }
//
//    @Bean(name = "pgTransactionManager")
//    public PlatformTransactionManager pgTransactionManager(
//            @Qualifier("pgEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//}