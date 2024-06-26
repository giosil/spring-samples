package org.dew.app;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.dew.app.jpa.ComuneEntity;

import org.dew.app.model.Comune;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
// @RequestMapping("/api")
public class AppController {
  
  Logger logger = LoggerFactory.getLogger(AppController.class);
  
  @Autowired
  AppService service;
  
  @GetMapping("/comuni")
  public List<Comune> find(@RequestParam(value = "name", defaultValue = "") String name) throws Exception {
    logger.info("AppController.find(" + name + ")...");
    
    List<Comune> result = service.findComuni(name);
    
    logger.info("AppController.find(" + name + ") -> [" + result.size() + "]");
    
    return result;
  }
  
  @GetMapping("/comuni/prov/{prov}")
  public List<ComuneEntity> findComuniByProv(@PathVariable String prov) throws Exception {
    logger.info("AppController.findComuniByProv(" + prov + ")...");
    
    List<ComuneEntity> result = service.findComuniByProvincia(prov);
    
    logger.info("AppController.findComuniByProv(" + prov + ") -> [" + result.size() + "]");
    
    return result;
  }
  
  @PostMapping("/comuni")
  public ComuneEntity create(@RequestBody ComuneEntity comune) {
    logger.info("AppController.create(" + comune + ")...");
    
    return service.insertComune(comune);
  }
  
  @GetMapping("/comuniAll/{page}/{size}")
  public Page<ComuneEntity> allComuni(@PathVariable int page, @PathVariable int size, @RequestParam(value = "sortBy", defaultValue = "descrizione") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
    logger.info("AppController.allComuni(" + page + "," + size + ")...");
    
    return service.getAllComuni(page, size, sortBy, sortDir);
  }
  
  @GetMapping("/comuni/{id}")
  public ComuneEntity read(@PathVariable String id) {
    logger.info("AppController.read(" + id + ")...");
    
    return service.getComuneById(id);
  }
  
  @PutMapping("/comuni")
  public ComuneEntity update(@RequestBody ComuneEntity comune) {
    logger.info("AppController.update(" + comune + ")...");
    
    return service.insertComune(comune);
  }
  
  @DeleteMapping("/comuni/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    logger.info("AppController.delete(" + id + ")...");
    
    service.deleteComune(id);
  }
  
  @GetMapping("/hello")
  public ResponseEntity<String> hello() {
    String responseBody = "Hello, World!";
    
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Content-Type", "text/plain");
    
    return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
  }
  
  @RequestMapping("/app-*")
  public void redirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
    
    request.getRequestDispatcher("/index.html").forward(request, response);
    
  }
}
