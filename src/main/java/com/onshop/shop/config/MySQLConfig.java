package com.onshop.shop.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	    basePackages = {

	            "com.onshop.shop.address",
	            "com.onshop.shop.article",
	            "com.onshop.shop.cart",
	            "com.onshop.shop.category",
	            "com.onshop.shop.config",
	            "com.onshop.shop.exception",
	            "com.onshop.shop.inventory",
	            "com.onshop.shop.order",
	            "com.onshop.shop.orderDetail",
	            "com.onshop.shop.payment",
	            "com.onshop.shop.product",
	            "com.onshop.shop.reply",
	            "com.onshop.shop.security",
	            "com.onshop.shop.seller",
	            "com.onshop.shop.store",
	            "com.onshop.shop.user",

	            "com.onshop.shop.util",
	            "com.onshop.shop.settlement",
	            "com.onshop.shop.review",
	            "com.onshop.shop.follow",
	            "com.onshop.shop.fileupload"

	        // ❌ "com.onshop.shop.vector" 제외

	    },
	    entityManagerFactoryRef = "mysqlEntityManagerFactory",
	    transactionManagerRef = "mysqlTransactionManager"
	)
public class MySQLConfig {

    @Bean(name = "mysqlDataSource")
    @Primary
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/final")
                .username("root")
                .password("1234")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Bean(name = "mysqlEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mysqlDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);

        return builder
                .dataSource(dataSource)
                .packages("com.onshop.shop") // 전체 entity 커버
                .persistenceUnit("mysql")
                .properties(properties)
                .build();
    }

    @Bean(name = "mysqlTransactionManager")
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}