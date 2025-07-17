package org.dew.app.report;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DB {
  
  protected static int QUERY_TIMEOUT = 10 * 60;
  
  public static
  String getString(Map<String, Object> parameters, String key, String defaultValue)
  {
    if(parameters == null) return defaultValue;
    
    Object value = parameters.get(key);
    
    if(value == null) return defaultValue;
    
    return value.toString();
  }
  
  public static
  List<List<Object>> select(Connection conn, Map<String, Object> parameters)
    throws Exception
  {
    if(parameters == null || parameters.isEmpty()) {
      System.err.println("[DB] Missing parameters");
      return new ArrayList<List<Object>>(0);
    }
    String table = toString(parameters.get("table"));
    if(table == null || table.length() == 0) {
      System.err.println("[DB] Missing table in parameters");
      return new ArrayList<List<Object>>(0);
    }
    
    Map<String, Object> mapFilter = toMap(parameters.get("filter"));
    List<String> listFields       = toListOfString(parameters.get("fields"));
    List<String> listGroupBy      = toListOfString(parameters.get("groupBy"));
    String  sOrderBy              = toString(parameters.get("orderBy"));
    int     iMaxRows              = toInt(parameters.get("maxRows"), 0);
    boolean boIncludeHeader       = toBoolean(parameters.get("headers"), true);
    
    Object paging = parameters.get("paging");
    if(paging != null) {
      if(mapFilter == null) mapFilter = new HashMap<String, Object>();
      mapFilter.put("__paging__", toBoolean(paging, false));
    }
    
    List<List<Object>> result = select(conn, table, mapFilter, listFields, sOrderBy, iMaxRows, boIncludeHeader);
    
    List<Integer> listIndex = indexOf(listFields, listGroupBy);
    if(listIndex != null && listIndex.size() > 0) {
      Map<String, List<Object>> mapGroupBy = new HashMap<String, List<Object>>();
      int row = 0;
      Iterator<List<Object>> iterator = result.iterator();
      while(iterator.hasNext()) {
        List<Object> record = iterator.next();
        row++;
        if(boIncludeHeader && row == 1) continue;
        
        String group = getGroupValues(record, listIndex);
        if(group == null || group.length() == 0) continue;
        
        List<Object> recordG = mapGroupBy.get(group);
        if(recordG == null) {
          mapGroupBy.put(group, record);
        }
        else {
          for(int j = 0; j < record.size(); j++) {
            if(listIndex.indexOf(j) >= 0) continue;
            concat(recordG, record, j);
          }
          iterator.remove();
        }
      }
    }
    
    return result;
  }
  
  public static
  List<List<Object>> select(Connection conn, String table, Map<String, Object> mapFilter, List<String> listFields, String sOrderBy, int iMaxRows, boolean boIncludeHeader)
    throws Exception
  {
    if(conn == null || table == null || table.length() == 0) {
      return new ArrayList<List<Object>>(0);
    }
    if(table.indexOf("--") >= 0 || table.indexOf('/') >= 0) {
      return new ArrayList<List<Object>>(0);
    }
    if(mapFilter == null) {
      mapFilter = new HashMap<String, Object>();
    }
    
    String sHint = "";
    if(sOrderBy != null && sOrderBy.startsWith("/*")) {
      int iEndHit = sOrderBy.indexOf("*/");
      sHint    = sOrderBy.substring(0, iEndHit + 2);
      sOrderBy = sOrderBy.substring(iEndHit + 2).trim();
      if(sHint != null && sHint.length() > 0) sHint += " ";
    }
    
    int iFltMaxRows = toInt(mapFilter.remove("__maxrows__"), 0);
    if(iFltMaxRows > 0) {
      iMaxRows = iFltMaxRows;
    }
    
    if(iMaxRows < 1) iMaxRows = 100000;
    
    int page = toInt(mapFilter.remove("__page__"), 0);
    
    boolean paging = toBoolean(mapFilter.remove("__paging__"), false);
    
    boolean descOrder = false;
    if(page < 0) {
      page = 0;
      descOrder = true;
    }
    
    String sAdditionalClause = null;
    Object oAdditionalClause = mapFilter.remove("_0");
    if(oAdditionalClause != null) {
      sAdditionalClause = oAdditionalClause.toString();
    }
    
    // Campi da convertire a boolean
    Set<String> setBlnFields = getBooleanFields(table,    mapFilter);
    
    String sFields = "*";
    if(listFields != null && listFields.size() > 0) {
      StringBuilder sbFields = new StringBuilder();
      for(int i = 0; i < listFields.size(); i++) {
        String sField = listFields.get(i);
        if(sField == null || sField.length() == 0) continue;
        sbFields.append("," + sField.toUpperCase().replace('-', '.'));
      }
      sFields = sbFields.substring(1);
    }
    
    String sSQL = null;
    if(table.startsWith("SELECT ") || table.startsWith("select ")) {
      sSQL = table;
      
      int iWhere = sSQL.indexOf("WHERE ");
      if(iWhere < 0) iWhere = sSQL.indexOf("where ");
      if(iWhere > 0) {
        String sWhere = buildWhere(mapFilter);
        if(sWhere != null && sWhere.length() > 0) {
          sSQL += " AND " + sWhere;
        }
      }
      else {
        String sWhere = buildWhere(mapFilter);
        if(sWhere != null && sWhere.length() > 0) {
          sSQL += " WHERE " + sWhere;
        }
      }
      
      if(sAdditionalClause != null && sAdditionalClause.length() > 0 && sAdditionalClause.indexOf("--") < 0 && sAdditionalClause.indexOf('/') < 0) {
        iWhere = sSQL.indexOf("WHERE ");
        if(iWhere < 0) iWhere = sSQL.indexOf("where ");
        if(iWhere > 0) {
          sSQL += " AND " + sAdditionalClause;
        }
        else {
          sSQL += " WHERE " + sAdditionalClause;
        }
      }
      
      if(sOrderBy != null && sOrderBy.length() > 0) {
        sSQL += " ORDER BY " + sOrderBy.replace('-', '.');
      }
      else {
        if(descOrder) {
          sSQL += " ORDER BY 1 DESC";
        }
        else {
          sSQL += " ORDER BY 1";
        }
      }
    }
    else {
      sSQL = "SELECT " + sHint + sFields + " FROM " + table;
      
      String sWhere = buildWhere(mapFilter);
      if(sWhere != null && sWhere.length() > 0) {
        sSQL += " WHERE " + sWhere;
      }
      
      if(sAdditionalClause != null && sAdditionalClause.length() > 0) {
        int iWhere = sSQL.indexOf("WHERE ");
        if(iWhere < 0) iWhere = sSQL.indexOf("where ");
        if(iWhere > 0) {
          sSQL += " AND " + sAdditionalClause;
        }
        else {
          sSQL += " WHERE " + sAdditionalClause;
        }
      }
      
      if(sOrderBy != null && sOrderBy.length() > 0) {
        sSQL += " ORDER BY " + sOrderBy.replace('-', '.');
      }
      else {
        if(descOrder) {
          sSQL += " ORDER BY 1 DESC";
        }
        else {
          sSQL += " ORDER BY 1";
        }
      }
    }
    
    Set<String> setBlnFieldNames = new HashSet<String>();
    Iterator<String> iterator = setBlnFields.iterator();
    while(iterator.hasNext()) {
      String fieldName = getFieldName(iterator.next());
      if(fieldName == null) continue;
      setBlnFieldNames.add(fieldName);
    }
    
    List<List<Object>> listResult = new ArrayList<List<Object>>();
    
    System.out.println("[DB] " + sSQL);
    
    long count = -1;
    int  rows  = 0;
    Statement stm = null;
    ResultSet rs = null;
    try {
      if(page > 0) {
        count = count(conn, sSQL);
      }
      
      stm = conn.createStatement();
      stm.setQueryTimeout(QUERY_TIMEOUT);
      rs  = stm.executeQuery(sSQL);
      
      if(page > 1 && iMaxRows > 0) {
        skip(rs, (page - 1) * iMaxRows);
      }
      
      Set<Integer> setBlnFieldIdxs = new HashSet<Integer>();
      
      List<Object> listHeader = new ArrayList<Object>();
      ResultSetMetaData rsmd = rs.getMetaData();
      int columnCount = rsmd.getColumnCount();
      int[] columnTypes = new int[columnCount];
      for(int i = 0; i < columnCount; i++) {
        String columnName = rsmd.getColumnName(i + 1);
        if(setBlnFieldNames.contains(columnName.toUpperCase())) {
          setBlnFieldIdxs.add(i);
        }
        listHeader.add(columnName);
        columnTypes[i] = rsmd.getColumnType(i + 1);
      }
      if(boIncludeHeader) {
        listResult.add(listHeader);  
      }
      
      while(rs.next()) {
        List<Object> listRecord = new ArrayList<Object>(columnCount);
        for(int i = 0; i < columnCount; i++) {
          int t = columnTypes[i];
          if(t == Types.BINARY || t == Types.BLOB || t == Types.CLOB) {
            listRecord.add("<BLOB>");
          }
          else {
            if(setBlnFieldIdxs.contains(i)) {
              listRecord.add(toBooleanObj(rs.getString(i + 1), false));
            }
            else {
              listRecord.add(rs.getString(i + 1));
            }
          }
        }
        listResult.add(listRecord);
        rows++;
        if(rows >= iMaxRows) break;
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      if(rs   != null) try{ rs.close();  } catch(Exception ex) {}
      if(stm  != null) try{ stm.close(); } catch(Exception ex) {}
    }
    
    if(paging) {
      if(listResult.size() > 0) {
        List<Object> item0 = listResult.get(0);
        if(item0 != null) {
          item0.add(count);
          item0.add(iMaxRows);
          if(iMaxRows > 0 && count > 0) {
            long pages = count / iMaxRows;
            if((count % iMaxRows) > 0) pages++;
            item0.add(pages);
          }
          else {
            item0.add(1);
          }
        }
      }
    }
    
    return listResult;
  }
  
  protected static
  String getFieldName(String fieldName)
  {
    if(fieldName == null) return fieldName;
    int sep = fieldName.lastIndexOf('.');
    if(sep < 0) sep = fieldName.lastIndexOf('-');
    if(sep <= 0) return fieldName;
    return fieldName.substring(sep+1).trim().toUpperCase();
  }
  
  protected static
  String replaceParameter(String sSQL, Object val)
  {
    if(sSQL == null || sSQL.length() == 0) {
      return sSQL;
    }
    int indexOf = sSQL.indexOf('?');
    if(indexOf < 0) return sSQL;
    
    String sSQL_1 = sSQL.substring(0, indexOf);
    String sSQL_2 = sSQL.substring(indexOf + 1);
    
    if(val == null) {
      return sSQL_1 + "NULL" + sSQL_2;
    }
    else if(val instanceof String) {
      return sSQL_1 + "'" + ((String) val).replace("'", "''") + "'" + sSQL_2;
    }
    else if(val instanceof Number) {
      return sSQL_1 + val + sSQL_2;
    }
    else if(val instanceof Date) {
      return sSQL_1 + toSQLDate((Date) val) + sSQL_2;
    }
    else if(val instanceof Calendar) {
      return sSQL_1 + toSQLDate((Calendar) val) + sSQL_2;
    }
    return sSQL_1 + "'" + val.toString().replace("'", "''") + "'" + sSQL_2;
  }
  
  protected static
  String buildWhere(Map<String, Object> mapFilter)
  {
    if(mapFilter == null || mapFilter.isEmpty()) return null;
    
    Object oTime = mapFilter.remove("__time__");
    Calendar calFromTime = null;
    Calendar calToTime   = null;
    if(oTime != null) {
      int iTime = toInt(oTime, 0);
      int iHH   = iTime / 100;
      int iMM   = iTime % 100;
      
      calFromTime = Calendar.getInstance();
      calFromTime.set(Calendar.HOUR_OF_DAY, iHH);
      calFromTime.set(Calendar.MINUTE,      iMM);
      calFromTime.set(Calendar.SECOND,        0);
      calFromTime.set(Calendar.MILLISECOND,   0);
      calFromTime.add(Calendar.MINUTE,      -10);
      
      calToTime = Calendar.getInstance();
      calToTime.set(Calendar.HOUR_OF_DAY, iHH);
      calToTime.set(Calendar.SECOND,        0);
      calToTime.set(Calendar.MILLISECOND,   0);
      calToTime.set(Calendar.MINUTE,      iMM);
      calToTime.add(Calendar.MINUTE,       10);
    }
    
    StringBuilder sbResult = new StringBuilder();
    Iterator<Map.Entry<String, Object>> iterator = mapFilter.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      Object oKey = entry.getKey();
      String sKey = oKey.toString();
      Object valueTmp = entry.getValue();
      if(valueTmp == null) continue;
      
      sKey = sKey.replace('-', '.');
      
      if(sKey.indexOf('/') >= 0) continue;
      
      boolean boStartsWithPerc = false;
      boolean boEndsWithPerc   = false;
      boStartsWithPerc = sKey.startsWith("x__");
      if(boStartsWithPerc) sKey = sKey.substring(3);
      boEndsWithPerc = sKey.endsWith("__x");
      if(boEndsWithPerc) sKey = sKey.substring(0, sKey.length() - 3);
      
      boolean boToUpper = sKey.endsWith("__u");
      if(boToUpper) sKey = sKey.substring(0, sKey.length() - 3);
      boolean boToLower = sKey.endsWith("__l");
      if(boToLower) sKey = sKey.substring(0, sKey.length() - 3);
      
      boolean boBAE  = sKey.endsWith("__bae"); // Begins AND Ends
      boolean boBOE  = sKey.endsWith("__boe"); // Begins OR  Ends
      boolean boNOB  = sKey.endsWith("__nob"); // Not Begins
      boolean boNOE  = sKey.endsWith("__noe"); // Not Ends
      boolean boGTE  = sKey.endsWith("__gte");
      boolean boLTE  = sKey.endsWith("__lte");
      boolean boNE   = sKey.endsWith("__neq");
      if(!boNE) boNE = sKey.endsWith("__not");
      if(boBAE || boBOE || boNOB || boNOE || boGTE || boLTE || boNE) {
        sKey = sKey.substring(0, sKey.length() - 5);
      }
      
      boolean boGT  = sKey.endsWith("__gt");
      boolean boLT  = sKey.endsWith("__lt");
      if(boGT || boLT) sKey = sKey.substring(0, sKey.length() - 4);
      
      boolean boLike = false;
      String value   = null;
      if(valueTmp instanceof String) {
        String s = ((String) valueTmp).trim();
        
        if(boToUpper) {
          s = s.trim().toUpperCase();
        }
        else if(boToLower) {
          s = s.trim().toLowerCase();
        }
        
        if(s.length() == 0) continue;
        if(s.equalsIgnoreCase("null")) {
          value = "NULL";
        }
        else {
          value = "'";
          if(boStartsWithPerc) value += "%";
          value += s.replace("'", "''");
          if(boEndsWithPerc) value += "%";
          value += "'";
        }
        
        if(boStartsWithPerc || boEndsWithPerc) {
          boLike = value.indexOf('%') >= 0;
        }
        
        // Is a date?
        char c0 = s.charAt(0);
        char cL = s.charAt(s.length()-1);
        if(!boLike && Character.isDigit(c0) && Character.isDigit(cL) && s.length() > 7 && s.length() < 11) {
          int iSep1 = s.indexOf('/');
          if(iSep1 < 0) {
            iSep1 = s.indexOf('-');
            // YYYY-MM-DD
            if(iSep1 != 4) iSep1 = -1;
          }
          if(iSep1 > 0) {
            int iSep2 = s.indexOf('/', iSep1 + 1);
            if(iSep2 < 0) {
              iSep2 = s.indexOf('-', iSep1 + 1);
              // YYYY-MM-DD
              if(iSep2 != 7) iSep1 = -1;
            }
            if(iSep2 > 0) {
              Calendar cal = toCalendar(s);
              if(cal != null) {
                if((boLT || boLTE) && calToTime != null) {
                  value = toSQLDate(cal, calToTime);
                }
                else if((boGT || boGTE) && calFromTime != null) {
                  value = toSQLDate(cal, calFromTime);
                }
                else if(boLTE) {
                  boLTE = false;
                  boLT  = true;
                  cal.add(Calendar.DATE, 1);
                  value = toSQLDate(cal);
                }
                else if(boLT) {
                  value = toSQLDate(cal);
                }
                else {
                  value = toSQLDate(cal);
                }
              }
            }
          }
        }
      }
      else if(valueTmp instanceof Calendar) {
        Calendar cal = (Calendar) valueTmp;
        if((boLT || boLTE) && calToTime != null) {
          value = toSQLDate(cal, calToTime);
        }
        else if((boGT || boGTE) && calFromTime != null) {
          value = toSQLDate(cal, calFromTime);
        }
        else if(boLTE) {
          boLTE = false;
          boLT  = true;
          cal.add(Calendar.DATE, 1);
          value = toSQLDate(cal);
        }
        else if(boLT) {
          value = toSQLDate(cal);
        }
        else {
          value = toSQLDate(cal);
        }
      }
      else if(valueTmp instanceof Date) {
        if((boLT || boLTE) && calToTime != null) {
          value = toSQLDate((Date) valueTmp, calToTime);
        }
        else if((boGT || boGTE) && calFromTime != null) {
          value = toSQLDate((Date) valueTmp, calFromTime);
        }
        else if(boLTE) {
          boLTE = false;
          boLT  = true;
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(((Date) valueTmp).getTime());
          cal.add(Calendar.DATE, 1);
          value = toSQLDate(cal);
        }
        else if(boLT) {
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(((Date) valueTmp).getTime());
          value = toSQLDate(cal);
        }
        else {
          value = toSQLDate((Date) valueTmp);
        }
      }
      else if(valueTmp instanceof Boolean) {
        value = "'" + decodeBoolean((Boolean) valueTmp) + "'";
      }
      else {
        value = valueTmp.toString();
      }
      
      if((boNOB || boNOE) && value.length() > 2) {
        sbResult.append(sKey.toUpperCase());
        sbResult.append(" NOT LIKE ");
        if(boNOB) {
          sbResult.append(value.substring(0, value.length()-1) + "%'");
        }
        else {
          sbResult.append("'%" + value.substring(1));
        }
        sbResult.append(" AND ");
        continue;
      }
      else if((boBAE || boBOE) && value.length() > 2) {
        sbResult.append("(");
        sbResult.append(sKey.toUpperCase());
        sbResult.append(" LIKE ");
        sbResult.append(value.substring(0, value.length()-1) + "%'");
        if(boBOE) {
          sbResult.append(" OR ");
        }
        else {
          sbResult.append(" AND ");
        }
        sbResult.append(sKey.toUpperCase());
        sbResult.append(" LIKE ");
        sbResult.append("'%" + value.substring(1));
        sbResult.append(") AND ");
        continue;
      }
      
      sbResult.append(sKey.toUpperCase());
      if(boNE) {
        sbResult.append(" <> ");
      }
      else if(boGT) {
        sbResult.append(" > ");
      }
      else if(boGTE) {
        sbResult.append(" >= ");
      }
      else if(boLT) {
        sbResult.append(" < ");
      }
      else if(boLTE) {
        sbResult.append(" <= ");
      }
      else if(boLike) {
        sbResult.append(" LIKE ");
      }
      else {
        sbResult.append('=');
      }
      sbResult.append(value);
      sbResult.append(" AND ");
    }
    String result = sbResult.toString();
    if(result.length() > 0) {
      // Remove last " AND " (5 length)
      result = result.substring(0, result.length()-5);
    }
    return result;
  }
  
  protected static
  long count(Connection conn, String sSQL)
    throws Exception
  {
    if(conn == null || sSQL == null || sSQL.length() == 0) {
      return 0l;
    }
    
    int iFrom = sSQL.indexOf("FROM");
    if(iFrom < 0) iFrom = sSQL.indexOf("from");
    if(iFrom < 0) iFrom = sSQL.indexOf("From");
    if(iFrom < 0) {
      sSQL  = "FROM " + sSQL;
      iFrom = 0;
    }
    
    int iGroupBy = sSQL.indexOf("GROUP BY");
    if(iGroupBy < 0) iGroupBy = sSQL.indexOf("group by");
    if(iGroupBy < 0) iGroupBy = sSQL.indexOf("Group by");
    
    int iOrderBy = sSQL.indexOf("ORDER BY");
    if(iOrderBy < 0) iOrderBy = sSQL.indexOf("order by");
    if(iOrderBy < 0) iOrderBy = sSQL.indexOf("Order by");
    
    String sSQL_C = "";
    if(iGroupBy > 0) {
      sSQL_C = "SELECT COUNT(*) FROM (" + sSQL + ")";
    }
    else {
      if(iOrderBy > 0) {
        sSQL = sSQL.substring(iFrom, iOrderBy).trim();
      }
      else {
        sSQL = sSQL.substring(iFrom).trim();
      }
      sSQL_C = "SELECT COUNT(*) " + sSQL;
    }
    
    long result = 0;
    Statement stm = null;
    ResultSet rs  = null;
    try {
      stm = conn.createStatement();
      stm.setQueryTimeout(QUERY_TIMEOUT);
      rs  = stm.executeQuery(sSQL_C);
      if(rs.next()) result = rs.getLong(1);
    }
    finally {
      if(rs  != null) try { rs.close();  } catch(Exception ex) {}
      if(stm != null) try { stm.close(); } catch(Exception ex) {}
    }
    return result;
  }
  
  protected static
  void skip(ResultSet rs, int numberOfElements)
    throws Exception
  {
    if(rs == null || numberOfElements < 1) {
      return;
    }
    int count = 0;
    while(rs.next()) {
      count++;
      if(count >= numberOfElements) break;
    }
  }
  
  protected static
  Set<String> getEncryptedFields(String table, Map<String, Object> mapFilter)
  {
    Set<String> result = new HashSet<String>();
    if(mapFilter == null) return result;
    for(int i = 1; i <= 10; i++) {
      Object field = mapFilter.remove("_" + i);
      if(field instanceof String) {
        String sField = (String) field;
        if(sField.length() > 0) {
          result.add(sField.trim().replace('-', '.').toUpperCase());
        }
      }
    }
    return result;
  }
  
  protected static
  Set<String> getBooleanFields(String table, Map<String, Object> mapFilter)
  {
    Set<String> result = new HashSet<String>();
    if(mapFilter == null) return result;
    for(int i = 1; i <= 10; i++) {
      Object field = mapFilter.remove("_b" + i);
      if(field instanceof String) {
        String sField = (String) field;
        if(sField.length() > 0) {
          result.add(sField.trim().replace('-', '.').toUpperCase());
        }
      }
    }
    return result;
  }
  
  protected static
  Set<String> getStringDateFields(String table, Map<String, Object> mapFilter)
  {
    Set<String> result = new HashSet<String>();
    if(mapFilter == null) return result;
    for(int i = 1; i <= 10; i++) {
      Object field = mapFilter.remove("_d" + i);
      if(field instanceof String) {
        String sField = (String) field;
        if(sField.length() > 0) {
          result.add(sField.trim().replace('-', '.').toUpperCase());
        }
      }
    }
    return result;
  }
  
  protected static
  byte[] getBLOBContent(ResultSet rs, int index)
    throws Exception
  {
    Blob blob = rs.getBlob(index);
    if(blob == null) return null;
    
    InputStream is = blob.getBinaryStream();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] abDataBuffer = new byte[1024];
    int iBytesRead = 0;
    while((iBytesRead = is.read(abDataBuffer, 0, abDataBuffer.length)) != -1) {
      baos.write(abDataBuffer, 0, iBytesRead);
    }
    baos.flush();
    
    return baos.toByteArray();
  }
  
  protected static
  Map<String, Object> toMap(ResultSet rs)
    throws Exception
  {
    Map<String, Object> mapResult = null;
    
    List<String> listFields = new ArrayList<String>();
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int iColumnCount = rsmd.getColumnCount();
      if(rs.next()) {
        mapResult = new HashMap<String, Object>();
        for(int i = 1; i <= iColumnCount; i++) {
          String sField  = rsmd.getColumnName(i);
          int iFieldType = rsmd.getColumnType(i);
          if(iFieldType == java.sql.Types.CHAR || iFieldType == java.sql.Types.VARCHAR) {
            mapResult.put(sField, rs.getString(i));
          }
          else if(iFieldType == java.sql.Types.DATE) {
            mapResult.put(sField, rs.getDate(i));
          }
          else if(iFieldType == java.sql.Types.TIME || iFieldType == java.sql.Types.TIMESTAMP) {
            mapResult.put(sField, rs.getTimestamp(i));
          }
          else if(iFieldType == java.sql.Types.BINARY || iFieldType == java.sql.Types.BLOB || iFieldType == java.sql.Types.CLOB) {
            mapResult.put(sField, getBLOBContent(rs, i));
          }
          else {
            String sValue = rs.getString(i);
            if(sValue != null) {
              if(sValue.indexOf('.') >= 0 || sValue.indexOf(',') >= 0) {
                mapResult.put(sField, rs.getDouble(i));
              }
              else {
                mapResult.put(sField, rs.getInt(i));
              }
            }
            else {
              mapResult.put(sField, null);
            }
          }
          listFields.add(sField);
        }
      }
    }
    finally {
      if(rs  != null) try{ rs.close();  } catch(Exception ex) {}
    }
    return mapResult;
  }
  
  protected static
  String getSQLMultiString(String field, String values)
  {
    String sqlValues = getSQLMultiString(values);
    if(sqlValues == null || sqlValues.length() == 0) {
      // Restituire stringa vuota (per capire se filtro e' valorizzato)
      return "";
    }
    if(sqlValues.indexOf(',') > 0) {
      return field + " IN (" + sqlValues + ")";
    }
    return field + "=" + sqlValues;
  }
  
  protected static
  String getSQLMultiString(String values)
  {
    if(values == null) {
      // Restituire stringa vuota (per capire se filtro e' valorizzato)
      return "";
    }
    values = values.trim();
    if(values.length() == 0) {
      // Restituire stringa vuota (per capire se filtro e' valorizzato)
      return "";
    }
    if(values.indexOf(',') >= 0) {
      return getSQLMultiString(values, ',');
    }
    if(values.indexOf(' ') >= 0) {
      return getSQLMultiString(values, ' ');
    }
    return "'" + values + "'";
  }
  
  protected static
  String getSQLMultiString(String values, char sep)
  {
    // Restituire stringa vuota (per capire se filtro e' valorizzato)
    if(values == null) return "";
    StringBuilder sb = new StringBuilder();
    int iIndexOf = 0;
    int iBegin   = 0;
    iIndexOf     = values.indexOf(sep);
    while(iIndexOf >= 0) {
      String value = values.substring(iBegin, iIndexOf).trim();
      if(value.length() > 0) {
        sb.append(",'" + value.replace("'", "''") + "'");
      }
      iBegin = iIndexOf + 1;
      iIndexOf = values.indexOf(sep, iBegin);
    }
    String value = values.substring(iBegin).trim();
    if(value.length() > 0) {
      sb.append(",'" + value.replace("'", "''") + "'");
    }
    if(sb.length() > 0) return sb.substring(1);
    // Restituire stringa vuota (per capire se filtro e' valorizzato)
    return "";
  }
  
  protected static
  String sqlInParams(String field, List<?> values)
  {
    if(field == null || field.length() == 0) {
      return "";
    }
    if(values == null) {
      return "";
    }
    int size = values.size();
    if(size == 0) {
      return "";
    }
    if(size == 1) {
      return field + "=?";
    }
    StringBuilder params = new StringBuilder();
    for(int i = 0; i < size; i++) {
      params.append(",?");
    }
    return field + " IN (" + params.substring(1) + ")";
  }
  
  @SuppressWarnings("unchecked")
  protected static
  Map<String, Object> toMap(Object value) 
  {
    if(value == null) return null;
    if(value instanceof Map) {
      return (Map<String, Object>) value;
    }
    return null;
  }
  
  protected static
  List<String> toListOfString(Object value) 
  {
    if(value == null) return null;
    List<String> result = new ArrayList<String>();
    if(value instanceof Collection) {
      Iterator<?> iterator = ((Collection<?>) value).iterator();
      while(iterator.hasNext()) {
        Object item = iterator.next();
        if(item == null) {
          result.add(null);
        }
        else {
          result.add(item.toString());
        }
      }
    }
    else if(value.getClass().isArray()) {
      int length = Array.getLength(value);
      for(int i = 0; i < length; i++) {
        Object item = Array.get(value, i);
        if(item == null) {
          result.add(null);
        }
        else {
          result.add(item.toString());
        }
      }
    }
    else if(value instanceof String) {
      String text = (String) value;
      if(text.startsWith("[") && text.endsWith("]")) {
        text = text.substring(1, text.length()-1);
      }
      int iIndexOf = text.indexOf(',');
      int iBegin   = 0;
      while(iIndexOf >= 0) {
        result.add(text.substring(iBegin, iIndexOf));
        iBegin = iIndexOf + 1;
        iIndexOf = text.indexOf(',', iBegin);
      }
      result.add(text.substring(iBegin));
    }
    else {
      result.add(value.toString());
    }
    return result;
  }
  
  protected static
  String toString(Object value) 
  {
    if(value == null) return null;
    return value.toString();
  }
  
  protected static
  int toInt(Object value, int defaultValue) 
  {
    if(value == null) return defaultValue;
    if(value instanceof Number) {
      return ((Number) value).intValue();
    }
    String s = value.toString().trim();
    try {
      return Integer.parseInt(s);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    return defaultValue;
  }
  
  protected static
  String toSQLDate(Calendar cal)
  {
    if(cal == null) return "NULL";
    
    int iYear   = cal.get(Calendar.YEAR);
    int iMonth  = cal.get(Calendar.MONTH) + 1;
    int iDay    = cal.get(Calendar.DAY_OF_MONTH);
    int iHour   = cal.get(Calendar.HOUR_OF_DAY);
    int iMinute = cal.get(Calendar.MINUTE);
    int iSecond = cal.get(Calendar.SECOND);
    String sMonth  = iMonth  < 10 ? "0" + iMonth  : String.valueOf(iMonth);
    String sDay    = iDay    < 10 ? "0" + iDay    : String.valueOf(iDay);
    String sHour   = iHour   < 10 ? "0" + iHour   : String.valueOf(iHour);
    String sMinute = iMinute < 10 ? "0" + iMinute : String.valueOf(iMinute);
    String sSecond = iSecond < 10 ? "0" + iSecond : String.valueOf(iSecond);
    return "TO_DATE('" + iYear + "-" + sMonth + "-" + sDay + " " + sHour + ":" + sMinute + ":" + sSecond + "','YYYY-MM-DD HH24:MI:SS')";
  }
  
  protected static
  String toSQLDate(Calendar cal, Calendar calTime)
  {
    if(cal == null) return "NULL";
    
    int iYear   = cal.get(Calendar.YEAR);
    int iMonth  = cal.get(Calendar.MONTH) + 1;
    int iDay    = cal.get(Calendar.DAY_OF_MONTH);
    int iHour   = 0;
    int iMinute = 0;
    int iSecond = 0;
    if(calTime != null) {
      iHour   = calTime.get(Calendar.HOUR_OF_DAY);
      iMinute = calTime.get(Calendar.MINUTE);
      iSecond = calTime.get(Calendar.SECOND);
    }
    else {
      iHour   = cal.get(Calendar.HOUR_OF_DAY);
      iMinute = cal.get(Calendar.MINUTE);
      iSecond = cal.get(Calendar.SECOND);
    }
    String sMonth  = iMonth  < 10 ? "0" + iMonth  : String.valueOf(iMonth);
    String sDay    = iDay    < 10 ? "0" + iDay    : String.valueOf(iDay);
    String sHour   = iHour   < 10 ? "0" + iHour   : String.valueOf(iHour);
    String sMinute = iMinute < 10 ? "0" + iMinute : String.valueOf(iMinute);
    String sSecond = iSecond < 10 ? "0" + iSecond : String.valueOf(iSecond);
    return "TO_DATE('" + iYear + "-" + sMonth + "-" + sDay + " " + sHour + ":" + sMinute + ":" + sSecond + "','YYYY-MM-DD HH24:MI:SS')";
  }
  
  protected static
  String toSQLDate(Date date)
  {
    if(date == null) return "NULL";
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int iYear  = cal.get(Calendar.YEAR);
    int iMonth = cal.get(Calendar.MONTH) + 1;
    int iDay   = cal.get(Calendar.DAY_OF_MONTH);
    String sMonth = iMonth < 10 ? "0" + iMonth : String.valueOf(iMonth);
    String sDay   = iDay   < 10 ? "0" + iDay   : String.valueOf(iDay);
    return "TO_DATE('" + iYear + "-" + sMonth + "-" + sDay + "','YYYY-MM-DD')";
  }
  
  protected static
  String toSQLDate(Date date, Calendar calTime)
  {
    if(date == null) return "NULL";
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    
    return toSQLDate(cal, calTime);
  }
  
  protected static
  String decodeBoolean(Boolean value)
  {
    if(value == null) return "";
    if(value.booleanValue()) return "1";
    return "0";
  }
  
  protected static
  String decodeBoolean(boolean value)
  {
    return value ? "1" : "0";
  }
  
  protected static
  boolean toBoolean(Object value, boolean defaultValue) 
  {
    if(value == null) return defaultValue;
    
    String text = value.toString();
    
    if(text.length() == 0) return defaultValue;
    
    char c0 = text.charAt(0);
    
    return "1TtYySsVv".indexOf(c0) >= 0;
  }
  
  protected static
  Boolean toBooleanObj(String value, Boolean defaultValue) 
  {
    if(value == null || value.length() == 0) return defaultValue;
    
    char c0 = value.charAt(0);
    
    return "1TtYySsVv".indexOf(c0) >= 0;
  }
  
  protected static
  Calendar toCalendar(String text) 
  {
    if(text == null || text.length() < 8) {
      return null;
    }
    List<Integer> listNumbers = getNumbers(text);
    if(listNumbers == null || listNumbers.size() == 0) {
      return null;
    }
    int yyyy = 0;
    int mm = 0;
    int dd = 0;
    if(listNumbers.size() < 3) {
      int yyyymmdd = listNumbers.get(0);
      if(yyyymmdd < 10000000 || yyyymmdd > 99991231) {
        return null;
      }
      yyyy = yyyymmdd / 10000;
      mm   = (yyyymmdd % 10000) / 100;
      dd   = (yyyymmdd % 10000) % 100;
    }
    else if(listNumbers.size() >= 3) { 
      int n0 = listNumbers.get(0);
      if(n0 >= 1000) {
        yyyy = n0;
        mm   = listNumbers.get(1);
        dd   = listNumbers.get(2);
      }
      else {
        yyyy = listNumbers.get(2);
        mm   = listNumbers.get(1);
        dd   = n0;
      }
    }
    if(yyyy < 1000 || yyyy > 9999) return null;
    if(mm < 1 || mm > 12) return null;
    if(dd < 1 || dd > 31) return null;
    return new GregorianCalendar(yyyy, mm-1, dd);
  }
  
  protected static
  List<Integer> getNumbers(String text) 
  {
    List<Integer> result = new ArrayList<Integer>();
    if(text == null || text.length() == 0) {
      return result;
    }
    StringBuilder curr = new StringBuilder();
    for(int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if(Character.isDigit(c)) {
        curr.append(c);
      }
      else if(curr.length() != 0) {
        try {
          int number = Integer.parseInt(curr.toString());
          result.add(number);
        }
        catch(Exception ex) {
          System.err.println("Exception in parsing " + curr + " in getNumbers(" + text + "): " + ex);
        }
        curr = new StringBuilder();
      }
    }
    if(curr.length() != 0) {
      try {
        int number = Integer.parseInt(curr.toString());
        result.add(number);
      }
      catch(Exception ex) {
        System.err.println("Exception in parsing " + curr + " in getNumbers(" + text + "): " + ex);
      }
    }
    return result;
  }
  
  protected static
  List<Integer> indexOf(List<String> list0, List<String> list1)
  {
    List<Integer> result = new ArrayList<Integer>();
    if(list0 == null || list0.size() == 0) {
      return result;
    }
    if(list1 == null || list1.size() == 0) {
      return result;
    }
    for(int i = 0; i < list1.size(); i++) {
      String itemI = list1.get(i);
      if(itemI == null || itemI.length() == 0) {
        continue;
      }
      for(int j = 0; j < list0.size(); j++) {
        String itemJ = list0.get(j);
        if(itemJ == null || itemJ.length() == 0) {
          continue;
        }
        if(itemJ.equalsIgnoreCase(itemI)) {
          if(!result.contains(j)) {
            result.add(j);
          }
        }
      }
    }
    return result;
  }
  
  protected static
  String getGroupValues(List<Object> record, List<Integer> indexes)
  {
    if(record == null || record.size() == 0) {
      return null;
    }
    if(indexes == null || indexes.size() == 0) {
      return null;
    }
    String result = "";
    for(int i = 0; i < indexes.size(); i++) {
      int x = indexes.get(i);
      if(x < 0 || x >= record.size()) continue;
      result += ":" + record.get(x);
    }
    if(result.length() > 0) {
      return result.substring(1);
    }
    return result;
  }
  
  protected static
  void concat(List<Object> record0, List<Object> record1, int index)
  {
    if(record0 == null || record0.size() <= index) {
      return;
    }
    if(record1 == null || record1.size() <= index) {
      return;
    }
    Object o0 = record0.get(index);
    Object o1 = record1.get(index);
    if(o1 == null) return;
    String s1 = o1.toString();
    if(s1.length() == 0) return;
    if(o0 == null) {
      record0.set(index, o1);
    }
    else {
      String s0 = o0.toString();
      if(s0.length() == 0) {
        record0.set(index, o1);
      }
      else {
        if(s0.equals(s1)) return;
        if(s0.startsWith(s1 + ",")) return;
        if(s0.endsWith("," + s1)) return;
        if(s0.indexOf("," + s1 + ",") > 0) return;
        record0.set(index, o0 + "," + o1);
      }
    }
  }
}
