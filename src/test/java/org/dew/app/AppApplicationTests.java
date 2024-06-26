package org.dew.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppApplicationTests {
  
  @Autowired
  AppService service;

  @Test
  void contextLoads() throws Exception {
    assertThat(service).isNotNull();
    
    // assertThat(service.findComuniByProvincia("FG")).isNotNull().isNotEmpty();
  }

}
