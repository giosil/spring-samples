package org.dew.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.dew.app.jte.PortalContent;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;

import gg.jte.TemplateOutput;

import org.springframework.ui.ExtendedModelMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppApplicationTests {
  
  @Autowired
  AppService service;

  protected Model mockModel;
  protected MockHttpServletRequest mockRequest;
  protected TemplateOutput mockTemplateOutput;

  @BeforeEach
  void setUp() {
    mockModel   = new ExtendedModelMap();
    
    mockRequest = new MockHttpServletRequest();
    mockRequest.setMethod("GET");
    mockRequest.setRequestURI("/app-info");
    mockRequest.addParameter("debug", "1");
    
    mockTemplateOutput = new MockTemplateOutput();
  }
  
  @Test
  void contextLoads() throws Exception {
    assertThat(service).isNotNull();
    
    // assertThat(service.findComuniByProvincia("FG")).isNotNull().isNotEmpty();
    
    AppController appController = new AppController();
    
    String app = appController.app(mockModel, mockRequest);
    assertThat(app).isNotNull();
    
    new PortalContent().writeTo(mockTemplateOutput);
    assertThat(new PortalContent().add().getBody()).isNotNull().isBlank();
    assertThat(new PortalContent().add((String) null).getBody()).isNotNull().isBlank();
    
    assertThat(new PortalContent().add("<p>x</p>").getBody()).isNotNull().isNotBlank();
    assertThat(new PortalContent("<h2>t</h2>").add("<p>x</p>").getBody()).isNotNull().isNotBlank();
  }

}
