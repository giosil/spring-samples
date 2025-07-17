package org.dew.app.report;

import java.sql.Connection;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
public class ReportController {
  
  @Autowired
  DataSource dataSource;
  
  @PostMapping("/select")
  public List<List<Object>> select(@RequestBody Map<String, Object> parameters) throws Exception {
    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      
      return DB.select(conn, parameters);
    }
    finally {
      if(conn != null) try { conn.close(); } catch(Exception ex) {}
    }
  }
  
  @PostMapping("/export")
  public Map<String, Object> export(@RequestBody Map<String, Object> parameters) throws Exception {
    String title  = DB.getString(parameters, "title", "Report");
    String type   = DB.getString(parameters, "type",  "xlsx");
    // Debug
    String copyTo = DB.getString(parameters, "copyTo", null);
    
    List<List<Object>> records = null;
    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      
      records = DB.select(conn, parameters);
    }
    finally {
      if(conn != null) try { conn.close(); } catch(Exception ex) {}
    }
    
    byte[] content = ExportAs.any(records, title, type);
    
    if(copyTo != null && copyTo.length() > 0) {
      String filePath = ExportAs.getDesktopPath(copyTo);
      ExportAs.saveContent(content, filePath);
    }
    
    String base64 = Base64.getEncoder().encodeToString(content);
    
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("content", base64);
    result.put("title",   title);
    result.put("type",    type);
    
    return result;
  }
}
