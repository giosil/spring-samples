package org.dew.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import gg.jte.Content;

import org.dew.app.jte.PortalContent;

@Controller
public class AppController {
  
  public static final String VERSION = String.valueOf(System.currentTimeMillis());
  
  @Autowired
  Environment environment;
  
  @GetMapping("/app-*")
  public String app(Model model, HttpServletRequest request) {
    // Top Menu
    model.addAttribute("menu",    getMenu(request));
    // Import scripts
    model.addAttribute("sources", isDebug(request) ? "" : ".min");
    model.addAttribute("version", VERSION);
    // src/main/jte/page.jte
    return "page";
  }
  
  @GetMapping("/html-*")
  public void redirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
    
    request.getRequestDispatcher("/index.html").forward(request, response);
    
  }
  
  protected Content getMenu(HttpServletRequest request) {
    return new PortalContent("<div id=\"nav1-items\"></div>");
  }
  
  protected boolean isDebug(HttpServletRequest request) {
    String debug = request.getParameter("debug");
    if(debug == null || debug.length() == 0) return !isProdProfile();
    return "1YSTyst".indexOf(debug.charAt(0)) >= 0;
  }
  
  protected boolean isProdProfile() {
    if(environment == null) {
      return false;
    }
    String[] activeProfiles = environment.getActiveProfiles();
    for(int i = 0; i < activeProfiles.length; i++) {
      String profile = activeProfiles[i];
      if("prod".equals(profile)) return true;
    }
    return false;
  }
}
