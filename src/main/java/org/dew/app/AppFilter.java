package org.dew.app;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;

import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletResponse;

public class AppFilter extends HttpFilter {

  private static final long serialVersionUID = 1L;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    
  }
  
  @Override
  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException {
  
    response.setHeader("Access-Control-Allow-Origin",      "*");
    response.setHeader("Access-Control-Allow-Methods",     "GET, POST, PUT, PATCH, DELETE, OPTIONS");
    response.setHeader("Access-Control-Allow-Headers",     "Content-Type, Authorization");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    
    chain.doFilter(request, response);
  }
  
  @Override
  public void destroy() {
    
  }
}
