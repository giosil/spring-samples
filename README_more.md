# Spring-samples

## Security

Enable JPA auditing:

```java
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfiguration {
  @Bean
  public AuditorAware<String> auditorProvider() {
    return new AuditorAware<String>() {
      @Override
      public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();
        return Optional.of(authentication.getName());
      }
    };
  }
}
```

To add security configuration with Oauth2:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
```

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
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

## Redis Cache Service

```xml
<dependency>
	<groupId>io.lettuce</groupId>
	<artifactId>lettuce-core</artifactId>
</dependency>
```

```java
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

@Service
public class CacheService {
  private ClientResources sharedResources = DefaultClientResources.create();

  private final StatefulRedisMasterReplicaConnection<String, String> connection;
  private final RedisClient redisClient;

  public CacheService(Environment environment) {
    redisClient = RedisClient.create(sharedResources);
    redisClient.setOptions(ClusterClientOptions.builder()
      .topologyRefreshOptions(
        ClusterTopologyRefreshOptions.builder()
          .enablePeriodicRefresh(Duration.of(5, ChronoUnit.MINUTES))
          .dynamicRefreshSources(true)
          .build()
      ).build());
    connection = MasterReplica.connect(redisClient, StringCodec.UTF8, RedisURI.create(environment.getProperty("application.redis-uri")));
    connection.setReadFrom(ReadFrom.REPLICA_PREFERRED);
  }

  public String get(String key) {
    try {
      return connection.sync().get(key);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public void put(String key, String value) {
    try {
      connection.sync().set(key, value);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void put(String key, String value, long expiresIn) {
    try {
      RedisCommands<String, String> sync = connection.sync();
      sync.set(key, value);
      sync.expire(key, expiresIn);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
```
