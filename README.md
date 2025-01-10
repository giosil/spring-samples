# Spring-samples

Spring-boot samples.

## Build frontend

Before install typescrpt, uglify-js and uglifycss

npm install -g typescript
npm install -g uglify-js
npm install -g uglifycss

To build:

npm run build

## Build application

mvn clean package

or

mvn clean package -DskipTests

## Run application

mvn spring-boot:run

mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8787"

or

java -jar ./target/app-0.0.1-SNAPSHOT.jar

## Run application with prod profile

mvn spring-boot:run -Dspring-boot.run.profiles=prod

or 

java -jar ./target/app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

## Build docker appbe image (Backend)

docker build -t appbe .

## Build docker appdb image (Database)

cd database

docker build -t appdb .

## Run erpbdb separately

docker run --name appdb-postgres -p 5432:5432 -d appdb

## Run postgres separately (empty database)

docker run --name appdb-postgres -p 5432:5432 -e POSTGRES_DB=appdb -e POSTGRES_USER=appdb -e POSTGRES_PASSWORD=passw0rd -d postgres:12

## Docker compose

docker compose -p "app-cluster" up --detach

## Security

To add security configuration:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

```java
package org.dew.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
  @Bean
  public static PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
    .csrf(AbstractHttpConfigurer::disable)
    .authorizeHttpRequests(authz -> 
      authz
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/**").authenticated()
        .anyRequest().authenticated()
    )
    .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails admin = User.builder().username("admin").password(passwordEncoder().encode("password")).build();
    UserDetails user  = User.builder().username("user").password(passwordEncoder().encode("password")).build();
    return new InMemoryUserDetailsManager(admin, user);
  }
}
```

```java
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// ...

public String getSubject() {
  Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  if(authentication != null && authentication.isAuthenticated()) {
    return authentication.getName(); 
  }
  return null;
}
```

To add security configuration with oauth:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.security.oauth</groupId>
	<artifactId>spring-security-oauth2</artifactId>
	<version>${spring-security-oauth2}</version>
</dependency>
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
  http.authorizeHttpRequests(authz -> 
    {
      try {
        authz.requestMatchers("**").permitAll();
        authz
          .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
          .anyRequest().authenticated().and().oauth2ResourceServer().jwt(); // deprecated
      } 
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  );
  http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // deprecated
  return http.build();
}
```

```java
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;

// ...

public String getSubject() {
  Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  if(authentication == null) return null;
  Jwt user = (Jwt) authentication.getPrincipal();
  if(user == null) return null;
  return user.getSubject();
}
```
