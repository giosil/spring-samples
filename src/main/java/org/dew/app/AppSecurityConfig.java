package org.dew.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AppSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
    .csrf(AbstractHttpConfigurer::disable)
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/private/**").authenticated()
        .anyRequest().permitAll()
        )
    .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    return new AppAuthenticationProvider();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

//  @Bean
//  public UserDetailsService userDetailsService() {
//    UserDetails admin = User.builder().username("admin").password(passwordEncoder().encode("password")).build();
//    UserDetails user  = User.builder().username("user").password(passwordEncoder().encode("password")).build();
//    return new InMemoryUserDetailsManager(admin, user);
//  }
}
