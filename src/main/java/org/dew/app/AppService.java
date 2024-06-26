package org.dew.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.dew.app.jpa.ComuneEntity;
import org.dew.app.jpa.ComunePASRepository;
import org.dew.app.jpa.ComuneRepository;

import org.dew.app.model.Comune;

@Service
public class AppService {
  
  Logger logger = LoggerFactory.getLogger(AppService.class);
  
  @Autowired
  DataSource dataSource;
  
  @Autowired
  private ComuneRepository comuneRepository;
  
  @Autowired
  private ComunePASRepository comunePASRepository;
  
  public List<Comune> findComuni(String name) throws Exception {
    if(name == null) name = "";
    name = name.trim().toUpperCase();
    
    logger.info("AppService.findComuni(" + name + ")...");
    
    Thread.sleep(1000);
    
    List<Comune> result = new ArrayList<Comune>();
    
    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    try {
      conn = dataSource.getConnection();
      
      pstm = conn.prepareStatement("SELECT * FROM ANA_COMUNI WHERE DESCRIZIONE LIKE ?");
      pstm.setString(1, "%" + name + "%");
      
      rs = pstm.executeQuery();
      while(rs.next()) {
        String idComune    = rs.getString("ID_COMUNE");
        String fiscale     = rs.getString("FISCALE");
        String provincia   = rs.getString("PROVINCIA");
        String descrizione = rs.getString("DESCRIZIONE");
        String idRegione   = rs.getString("ID_REGIONE");
        
        result.add(new Comune(idComune, descrizione, fiscale, provincia, idRegione));
      }
    }
    finally {
      if(rs   != null) try { rs.close();   } catch(Exception ex) {}
      if(pstm != null) try { pstm.close(); } catch(Exception ex) {}
      if(conn != null) try { conn.close(); } catch(Exception ex) {}
    }
    
    logger.info("AppService.findComuni(" + name + ") -> [" + result.size() + "]");
    
    return result;
  }
  
  public List<ComuneEntity> findComuniByProvincia(String prov) throws Exception {
    return comuneRepository.findByProvincia(prov != null ? prov.trim().toUpperCase() : "");
  }
  
  public List<ComuneEntity> getAllComuni() {
    return comuneRepository.findAll();
  }
  
  public Page<ComuneEntity> getAllComuni(int page, int size, String sortBy, String sortDir) {
    
    Sort.Direction direction = Sort.Direction.fromString(sortDir);
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    
    return comunePASRepository.findAll(pageable);
  }
  
  public ComuneEntity getComuneById(String id) {
    return comuneRepository.findById(id).orElse(null);
  }
  
  public ComuneEntity insertComune(ComuneEntity comune) {
    return comuneRepository.save(comune);
  }
  
  public ComuneEntity updateComune(ComuneEntity comune) {
    String id = comune.getIdComune();
    if(id == null || id.isBlank()) {
      throw new RuntimeException("Invalid comune.id");
    }
    
    comune = comuneRepository.findById(id).orElseThrow(() -> new RuntimeException("Comune not found"));
    
    return comuneRepository.save(comune);
  }
  
  public void deleteComune(String id) {
    comuneRepository.deleteById(id);
  }
}
