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

To add security configuration with OAuth 2.0:

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

## Security OAuth 2.0 with Filter

```xml
<dependency>
	<groupId>com.auth0</groupId>
	<artifactId>java-jwt</artifactId>
	<version>4.5.0</version>
</dependency>
```

```java
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class SecurityFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = getToken(request);
		if(token != null && token.length() > 0) {
			String subject = getSubject(token);
			
			UserDetails userDetails = User.withUsername(subject).password("").roles("oper").build();
			
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		filterChain.doFilter(request, response);
	}
	
	protected String getToken(HttpServletRequest request) 
			throws ServletException, IOException {
		String authorization = request.getHeader("Authorization");
		if(authorization != null && authorization.startsWith("Bearer ")) {
			return authorization.substring(7);
		}
		return request.getParameter("token");
	}
	
	protected String getSubject(String token) {
		if(token == null || token.length() == 0) {
			return null;
		}
		DecodedJWT decodedJWT = JWT.decode(token);
		if(decodedJWT == null) return null;
		// Claim claimX5C = decodedJWT.getHeaderClaim("x5c");
		// Map<String, Claim> mapClaims = decodedJWT.getClaims();
		return decodedJWT.getSubject();
	}
}
```

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.addFilterBefore(new SecurityFilter(), UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(authz -> authz
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.anyRequest().authenticated()
			);
		return http.build();
	}
}
```

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoRestController {
	
	Logger logger = LoggerFactory.getLogger(DemoRestController.class);
	
	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "") String name, @AuthenticationPrincipal UserDetails userDetails) throws Exception {
		logger.info("DemoRestController.hello(" + name + ")...");
		if(name == null || name.length() == 0) {
			throw new Exception("Invalid name");
		}
		String result = null;
		if(userDetails != null) {
			result = "Hello " + name + " from " + userDetails.getUsername() + "!";
		}
		else {
			result = "Hello " + name + "!";
		}
		logger.info("DemoRestController.hello(" + name + ") -> + " + result);
		return result;
	}
}
```