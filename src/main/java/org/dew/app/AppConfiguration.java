package org.dew.app;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class AppConfiguration {
  
  @Bean
  public DataSource dataSource() {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName(env("APP_DS_DRIVER", "org.postgresql.Driver"));
    dataSourceBuilder.url(env("APP_DS_URL",                "jdbc:postgresql://localhost:5432/appdb"));
    dataSourceBuilder.username(env("APP_DS_USER",          "appdb"));
    dataSourceBuilder.password(env("APP_DS_PASS",          "passw0rd"));
    
    return dataSourceBuilder.build();
  }
  
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource());
    em.setPackagesToScan(this.getClass().getPackageName() + ".jpa"); // Entities
    
    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);
    
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("hibernate.dialect",             env("APP_JPA_DIALECT", "org.hibernate.dialect.PostgreSQLDialect"));
    
    // properties.put("hibernate.dialect",          "org.hibernate.dialect.PostgreSQLDialect");
    // properties.put("hibernate.hbm2ddl.auto",     "update");
    // properties.put("hibernate.show_sql",         "true");
    // properties.put("hibernate.format_sql",       "true");
    // properties.put("hibernate.use_sql_comments", "true");
    
    em.setJpaPropertyMap(properties);
    
    return em;
  }
  
  @Bean
  public ServletRegistrationBean<AppServlet> servletRegistration() {
    return new ServletRegistrationBean<>(new AppServlet(), "/home");
  }
  
  @Bean
  public FilterRegistrationBean<AppFilter> filterRegistration() {
    FilterRegistrationBean<AppFilter> registrationBean = new FilterRegistrationBean<>();
    
    registrationBean.setFilter(new AppFilter());
    registrationBean.addUrlPatterns("/comuni*");
    registrationBean.addUrlPatterns("/hello");
    
    return registrationBean;
  }
  
  public String env(String key, String defaultValue) {
    String value = System.getenv(key);
    if(value == null || value.length() == 0) {
      value = System.getProperty(key);
    }
    if(value == null || value.length() == 0) {
      return defaultValue;
    }
    return value;
  }
}
