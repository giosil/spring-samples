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

```java
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditorEntity {

  @CreatedBy
  @Column(name = "CREATION_BY")
  private String creationAuthor;

  @CreatedDate
  @Column(name = "CREATION_DATE", updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime creationDate;

  @LastModifiedBy
  @Column(name = "LAST_UPDATE_BY")
  private String updateAuthor;

  @LastModifiedDate
  @Column(name = "LAST_UPDATE_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime updateDate;

  public String getCreationAuthor() {
    return creationAuthor;
  }

  public void setCreationAuthor(String creationAuthor) {
    this.creationAuthor = creationAuthor;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public String getUpdateAuthor() {
    return updateAuthor;
  }

  public void setUpdateAuthor(String updateAuthor) {
    this.updateAuthor = updateAuthor;
  }

  public LocalDateTime getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(LocalDateTime updateDate) {
    this.updateDate = updateDate;
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

## Security OAuth 2.0 with Filter

Dependencies:

```xml
<dependency>
  <groupId>com.auth0</groupId>
  <artifactId>java-jwt</artifactId>
  <version>4.5.0</version>
</dependency>
```

Filter:

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
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class SecurityFilter extends OncePerRequestFilter {
  
  private final DataSource dataSource;
  
  private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
  private final static int CACHE_EXPIRE_AFTER = 300000;
  private final static int CACHE_CLEAN_AFTER  = 200;
  
  private final static String      DEFAULT_ROLE = "EMPLOYEE";
  private final static UserDetails DEFAULT_USER = User.withUsername("guest").password("").roles(DEFAULT_ROLE).build();
  
  public SecurityFilter(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // Check cache
    long now = System.currentTimeMillis();
    if(cache.size() > CACHE_CLEAN_AFTER) {
      // Clean cache
      cache.entrySet().removeIf(entry -> entry.getValue().expiryTime < now);
    }
    
    UserDetails userDetails = null;
    
    // Authentication
    String subject = getSubject(request);
    if(subject == null || subject.length() == 0) {
      // Generally in OPTIONS method calls
      userDetails = DEFAULT_USER;
    }
    else {
      // Get or build  userDetails
      CacheEntry cacheEntry = cache.get(subject);
      if(cacheEntry != null && cacheEntry.expiryTime > now) {
        // Valid cache entry
        userDetails = cacheEntry.userDetails;
      }
      else {
        String[] roles = getRoles(subject);
        userDetails = User.withUsername(subject).password("").roles(roles).build();
        if(cacheEntry != null) {
          // Update cache entry
          cacheEntry.setUserDetails(userDetails);
        }
        else {
          // Put new cache entry
          cache.put(subject, new CacheEntry(userDetails));
        }
      }
    }
    
    // Create spring authentication token
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    
    // Update security context
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    filterChain.doFilter(request, response);
    
    // Audit (Trace)
    insLog(subject, request, response);
  }
  
  protected String[] getRoles(String subject) {
    if(subject == null || subject.length() == 0) {
      return new String[] { DEFAULT_ROLE };
    }
    Date currentDate = new Date(System.currentTimeMillis());
    List<String> roles = new ArrayList<String>();
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = dataSource.getConnection();
      pstm = conn.prepareStatement("SELECT ROLE FROM USERS WHERE SUBJECT=? AND START_VALIDITY<=? AND END_VALIDITY>?");
      pstm.setString(1, subject.toUpperCase());
      pstm.setDate(2, currentDate);
      pstm.setDate(3, currentDate);
      rs = pstm.executeQuery();
      while(rs.next()) {
        String role = rs.getString("ROLE");
        if(!roles.contains(role)) roles.add(role);
      }
    }
    catch(SQLException sqlex) {
      System.err.println("[SecurityFilter] exception in getRoles(" + subject + "): " + sqlex);
    }
    finally {
      if(rs   != null) try { rs.close();   } catch(SQLException sqlex) { sqlex.printStackTrace(); }
      if(pstm != null) try { pstm.close(); } catch(SQLException sqlex) { sqlex.printStackTrace(); }
      if(conn != null) try { conn.close(); } catch(SQLException sqlex) { sqlex.printStackTrace(); }
    }
    if(roles.size() == 0) {
      return new String[] { DEFAULT_ROLE };
    }
    return roles.stream().toArray(String[]::new);
  }
  
  protected void insLog(String subject, HttpServletRequest request, HttpServletResponse response) {
    if(subject == null || subject.length() == 0) {
      return;
    }
    String method = request.getMethod();
    if("GET".equals(method) || "OPTIONS".equals(method)) return;
    String path   = request.getServletPath();
    String params = request.getQueryString();
    
    if(path    == null) path    = "/";
    if(params  == null) params  = "";
    if(subject.length() > 16)  subject = subject.substring(0, 16).toUpperCase();
    if(path.length()    > 250) path    = path.substring(0, 250);
    if(params.length()  > 250) params  = params.substring(0, 250);
    
    int status = response.getStatus();
    if(status >= 400) method = method + "*";
    
    Connection conn = null;
    PreparedStatement pstm = null;
    try {
      conn = dataSource.getConnection();
      pstm = conn.prepareStatement("INSERT INTO LOG_AUDIT(SUBJECT,LOG_DATE,LOG_METHOD,LOG_PATH,LOG_PARAMS) VALUES(?,?,?,?,?)");
      pstm.setString(1,    subject.toUpperCase());
      pstm.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
      pstm.setString(3,    method);
      pstm.setString(4,    path);
      pstm.setString(5,    params);
      pstm.executeUpdate();
    }
    catch(SQLException sqlex) {
      System.err.println("[SecurityFilter] exception in insLog(" + subject + "): " + sqlex);
    }
    finally {
      if(pstm != null) try { pstm.close(); } catch(SQLException sqlex) { sqlex.printStackTrace(); }
      if(conn != null) try { conn.close(); } catch(SQLException sqlex) { sqlex.printStackTrace(); }
    }
  }
  
  protected String getSubject(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if(authorization == null || authorization.length() == 0) return null;
    if(authorization.startsWith("Basic ")) {
      String basicAuth64 = authorization.substring(6);
      try {
        byte[] decoded = Base64.getDecoder().decode(basicAuth64);
        if(decoded == null || decoded.length == 0) {
          return null;
        }
        String basicAuth = new String(decoded);
        int sep = basicAuth.indexOf(':');
        if(sep < 0) sep = basicAuth.length();
        return basicAuth.substring(0, sep);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    else if(authorization.startsWith("Bearer ")) {
      try {
        String token = authorization.substring(7);
        if(token.length() < 30) return token;
        DecodedJWT decodedJWT = JWT.decode(token);
        if(decodedJWT == null) return null;
        return decodedJWT.getSubject();
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    return null;
  }
  
  private static class CacheEntry {
    UserDetails userDetails;
    long expiryTime;

    public CacheEntry(UserDetails userDetails) {
      this.userDetails = userDetails;
      this.expiryTime  = System.currentTimeMillis() + CACHE_EXPIRE_AFTER;
    }
    
    public void setUserDetails(UserDetails userDetails) {
      this.userDetails = userDetails;
      this.expiryTime  = System.currentTimeMillis() + CACHE_EXPIRE_AFTER;
    }
  }
}
```

Security configuration:

```java
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
  @Autowired
  DataSource dataSource;
  
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    
    http.csrf(AbstractHttpConfigurer::disable)
      .addFilterBefore(new SecurityFilter(dataSource), UsernamePasswordAuthenticationFilter.class)
      .authorizeHttpRequests(authz -> 
        authz
          .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
          .anyRequest().authenticated()
      )
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // or ALWAYS
    
    return http.build();
  }
}
```

Rest controller:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    
    // Alternatively:
    // 
    // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // String subjectName = authentication.getName();
    
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

## Client rest in backend

Dependency:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

WebClient:

```java
WebClient webClient = WebClient.builder().baseUrl("http://localhost:8081/api").build();

UserDTO userDTO = webClient
  .get()
  .uri(uriBuilder -> uriBuilder
    .path("/users/read")
    .queryParam("user", subject) 
    .build())
  .retrieve()
  .bodyToMono(UserDTO.class)
  .timeout(Duration.ofSeconds(20))
  .block();

// UserDTO to JSON

ObjectMapper objectMapper = new ObjectMapper();
String json = objectMapper.writeValueAsString(userDTO);
System.out.println(json);
```