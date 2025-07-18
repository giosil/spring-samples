package org.dew.app.report;

import java.util.HashMap;
import java.util.Map;

public class ReportCollection {
  
  public static
  Map<String, Object> getParameters(String report) 
  {
    Map<String, Object> result = new HashMap<String, Object>();
    
    if(report == null || report.length() == 0) {
      return result;
    }
    
    if(report.equalsIgnoreCase("log")) {
      result.put("table",   "HCMSYS_LOG");
      result.put("title",   "Report Operazioni");
      result.put("fields",  "CODICE_FISCALE,LOG_OPERAZIONE,LOG_FUNZIONE");
      result.put("clause",  "LOG_ECCEZIONE IS NULL");
      result.put("orderBy", "LOG_DATA DESC");
      result.put("maxRows", 50);
      result.put("headers", Boolean.FALSE);
      result.put("paging",  Boolean.FALSE);
      result.put("groupBy", "");
    }
    
    return result;
  }

}
