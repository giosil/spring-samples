package org.dew.app.report;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
public class ReportController {
  
  @Autowired
  ReportService reportService;
  
  @PostMapping("/select")
  public List<List<Object>> select(@RequestBody Map<String, Object> parameters) throws Exception {
    
    return reportService.select(parameters);
    
  }
  
  @PostMapping("/export")
  public Map<String, Object> export(@RequestBody Map<String, Object> parameters) throws Exception {
    
    List<List<Object>> records = reportService.select(parameters);
    
    return ReportUtils.exportMap(records, parameters);
  }
}
