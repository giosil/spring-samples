package org.dew.app;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class AppAuthenticationProvider implements AuthenticationProvider {

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();
    if ("admin".equals(username)) {
      if("password".equals(password)) {
        UserDetails userDetails = User.withUsername(username)
            .password(password)
            .authorities("admin")
            .build();
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
      }
      return null; // 401 Unauthorized (repeat login) 
    }
    throw new RuntimeException("Authentication failed");
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
