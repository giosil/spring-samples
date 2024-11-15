package org.dew.app;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.env.Environment;

import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class AppConfiguration {
  
  @Autowired
  private Environment environment;
  
  @Bean
  public DataSource dataSource() {
    DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName(getDataSourceDriver());
    dataSourceBuilder.url(getDataSourceURL());
    dataSourceBuilder.username(getDataSourceUser());
    dataSourceBuilder.password(getDataSourcePass());
    
    return dataSourceBuilder.build();
  }
  
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource());
    em.setPackagesToScan("org.dew.app.jpa"); // Entities
    
    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);
    
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("hibernate.dialect", getHibernateDialect());
    
    if(!isProdProfile()) {
      properties.put("hibernate.show_sql",         "true");
      properties.put("hibernate.format_sql",       "true");
      properties.put("hibernate.use_sql_comments", "true");
    }
    // properties.put("hibernate.hbm2ddl.auto",     "update");
    
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
  
  protected boolean isProdProfile() {
    if(environment == null) {
      return false;
    }
    String[] activeProfiles = environment.getActiveProfiles();
    if(activeProfiles == null || activeProfiles.length == 0) {
      return false;
    }
    for(int i = 0; i < activeProfiles.length; i++) {
      String profile = activeProfiles[i];
      if("prod".equals(profile)) return true;
    }
    return false;
  }
  
  protected String getDataSourceDriver() {
    return env("APP_DS_DRIVER", "org.postgresql.Driver");
  }
  
  protected String getDataSourceURL() {
    return env("APP_DS_URL", "jdbc:postgresql://localhost:5432/appdb");
  }
  
  protected String getDataSourceUser() {
    return env("APP_DS_USER", "appdb");
  }
  
  protected String getDataSourcePass() {
    return env("APP_DS_PASS", "passw0rd");
  }
  
  protected String getHibernateDialect() {
    String result = env("APP_JPA_DIALECT", null);
    if(result != null && result.length() > 0) return result;
    String url = getDataSourceURL();
    if(url == null || url.length() == 0) return "";
    if(url.indexOf("oracle:")      >= 0) return "org.hibernate.dialect.OracleDialect";
    if(url.indexOf("h2:")          >= 0) return "org.hibernate.dialect.H2Dialect";
    if(url.indexOf("hsqldb:")      >= 0) return "org.hibernate.dialect.HSQLDialect";
    if(url.indexOf("postgresql:")  >= 0) return "org.hibernate.dialect.PostgreSQLDialect";
    if(url.indexOf("mysql:")       >= 0) return "org.hibernate.dialect.MySQLDialect";
    if(url.indexOf("mariadb:")     >= 0) return "org.hibernate.dialect.MySQLDialect";
    return "";
  }
}
