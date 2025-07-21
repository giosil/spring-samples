package org.dew.app.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public
class ReportUtils
{
  public static String  YES_VALUE      = "X";
  public static String  NO_VALUE       = "";
  
  public static int     BOOL_COL_WIDTH = 4000;
  public static int     NUM_COL_WIDTH  = 5000;
  public static int     DATE_COL_WIDTH = 6000;
  public static int     STR_COL_WIDTH  = 8000;
  
  public static boolean CELL_BORDER    = false;
  public static boolean HEADER_GRAY    = false;
  
  public static String DATE_FORMAT    = "dd/mm/yyyy";
  
  public static String CSV_SEPARATOR  = ";";
  public static String CSV_DELIMITER  = "";
  
  public static
  Map<String, Object> exportMap(List<List<Object>> listData, Map<String, Object> parameters)
  {
    String title  = getString(parameters, "title", "Report");
    String type   = getString(parameters, "type",  "xlsx");
    String copyTo = getString(parameters, "copyTo", null);
    
    byte[] content = export(listData, title, type);
    
    if(copyTo != null && copyTo.length() > 0) {
      String filePath = copyTo;
      if(copyTo.indexOf('/') < 0 && copyTo.indexOf('\\') < 0) {
        filePath = ReportUtils.getDesktopPath(copyTo);
      }
      try {
        saveContent(content, filePath);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    
    int rows = listData != null ? listData.size() : 0;
    
    String base64 = Base64.getEncoder().encodeToString(content);
    
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("content", base64);
    result.put("title",   title);
    result.put("type",    type);
    result.put("rows",    rows);
    
    return result;
  }
  
  public static
  byte[] export(List<List<Object>> listData, Map<String, Object> parameters)
  {
    String title  = getString(parameters, "title", "Report");
    String type   = getString(parameters, "type",  "xlsx");
    String copyTo = getString(parameters, "copyTo", null);
    
    byte[] result = export(listData, title, type);
    
    if(copyTo != null && copyTo.length() > 0) {
      String filePath = copyTo;
      if(copyTo.indexOf('/') < 0 && copyTo.indexOf('\\') < 0) {
        filePath = ReportUtils.getDesktopPath(copyTo);
      }
      try {
        saveContent(result, filePath);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    
    return result;
  }
  
  public static
  byte[] export(List<List<Object>> listData, String title, String type)
  {
    if(type == null || type.length() == 0) {
      return csv(listData);
    }
    String typeLC = type.toLowerCase();
    if(typeLC.endsWith("xls") || typeLC.endsWith("excel")) {
      return xls(listData, title);
    }
    else if(typeLC.endsWith("xlsx") || typeLC.endsWith("sheet")) {
      return xlsx(listData, title);
    }
    return csv(listData);
  }
  
  public static
  byte[] xls(List<List<Object>> listData, String title)
  {
    Workbook workBook = new HSSFWorkbook();
    
    return fill(workBook, listData, title);
  }
  
  public static
  byte[] xlsx(List<List<Object>> listData, String title)
  {
    Workbook workBook = new XSSFWorkbook();
    
    return fill(workBook, listData, title);
  }
  
  public static
  byte[] csv(List<List<Object>> listData)
  {
    if(listData == null || listData.size() == 0) {
      return "".getBytes();
    }
    StringBuilder sb = new StringBuilder(listData.size() * 10);
    for(int i = 0; i < listData.size(); i++) {
      List<Object> listRecord = listData.get(i);
      if(listRecord == null) continue;
      sb.append(csvRow(listRecord));
      sb.append((char) 13);
      sb.append((char) 10);
    }
    return sb.toString().getBytes();
  }
  
  public static
  byte[] csv(List<List<Object>> listData, String title)
  {
    if(listData == null || listData.size() == 0) {
      return "".getBytes();
    }
    StringBuilder sb = new StringBuilder(listData.size() * 10);
    for(int i = 0; i < listData.size(); i++) {
      List<Object> listRecord = listData.get(i);
      if(listRecord == null) continue;
      sb.append(csvRow(listRecord));
      sb.append((char) 13);
      sb.append((char) 10);
    }
    return sb.toString().getBytes();
  }
  
  private static
  String getString(Map<String, Object> parameters, String key, String defaultValue)
  {
    if(parameters == null) return defaultValue;
    Object value = parameters.get(key);
    if(value == null) return defaultValue;
    return value.toString();
  }
  
  private static
  byte[] fill(Workbook workBook, List<List<Object>> listData, String title)
  {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    if(title == null || title.length() == 0) {
      title = "export";
    }
    Map<String, CellStyle> mapStyles = createStyles(workBook);
    Sheet sheet = workBook.createSheet(title);
    if(listData == null || listData.size() == 0) {
      try {
        workBook.write(result);
      }
      catch(Exception ex) {
        System.err.println("ExportAs.excel: " + ex);
      }
      return result.toByteArray();
    }
    Row row = sheet.createRow(0);
    // Header
    List<Object> listHeader = listData.get(0);
    for(int c = 0; c < listHeader.size(); c++) {
      createCell(sheet, row, c, mapStyles.get("headerl"), listHeader.get(c));
    }
    // Set Column Width
    if(listData.size() > 1) {
      List<Object> listFirstRow = listData.get(1);
      if(listFirstRow == null) listFirstRow = new ArrayList<Object>(0);
      for(int c = 0; c < listHeader.size(); c++) {
        if(c < listFirstRow.size()) {
          Object value = listFirstRow.get(c);
          if(value instanceof Number) {
            sheet.setColumnWidth(c, NUM_COL_WIDTH);
          }
          else if(value instanceof Boolean) {
            sheet.setColumnWidth(c, BOOL_COL_WIDTH);
          }
          else if(value instanceof Date) {
            sheet.setColumnWidth(c, DATE_COL_WIDTH);
          }
          else if(value instanceof Calendar) {
            sheet.setColumnWidth(c, DATE_COL_WIDTH);
          }
          else {
            sheet.setColumnWidth(c, STR_COL_WIDTH);
          }
        }
        else {
          sheet.setColumnWidth(c, NUM_COL_WIDTH);
        }
      }
    }
    else {
      for(int c = 0; c < listHeader.size(); c++) {
        sheet.setColumnWidth(c, NUM_COL_WIDTH);
      }
    }
    // Body
    for(int r = 1; r < listData.size(); r++) {
      row = sheet.createRow(r);
      List<Object> listRecord = listData.get(r);
      if(listRecord == null) continue;
      for(int c = 0; c < listRecord.size(); c++) {
        Object value = listRecord.get(c);
        if(value instanceof Number) {
          // Right Horizontal Alignment (r)
          createCell(sheet, row, c, mapStyles.get("whiter"), value);
        }
        else if(value instanceof Boolean) {
          // Center Horizontal Alignment (c)
          createCell(sheet, row, c, mapStyles.get("whitec"), value);
        }
        else if(value instanceof Date) {
          // Left Horizontal Alignment width Date Format (d)
          createCell(sheet, row, c, mapStyles.get("whited"), value);
        }
        else if(value instanceof Calendar) {
          // Left Horizontal Alignment width Date Format (d)
          createCell(sheet, row, c, mapStyles.get("whited"), value);
        }
        else {
          // Left Horizontal Alignment (l)
          createCell(sheet, row, c, mapStyles.get("whitel"), value);
        }
      }
    }
    try {
      workBook.write(result);
    }
    catch(Exception ex) {
      ReportService.log("ReportUtils", "fill: " + ex);
    }
    return result.toByteArray();
  }
  
  private static
  Cell createCell(Sheet sheet, Row row, int iCol, CellStyle cellStyle, Object value)
  {
    Cell cell = row.createCell(iCol);
    cell.setCellStyle(cellStyle);
    if(value == null) {
      cell.setCellValue("");
    }
    else if(value instanceof Date) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(((Date) value).getTime());
      cell.setCellValue(cal);
    }
    else if(value instanceof Calendar) {
      cell.setCellValue((java.util.Calendar) value);
    }
    else if(value instanceof Number) {
      cell.setCellValue(((Number) value).doubleValue());
    }
    else if(value instanceof Boolean) {
      cell.setCellValue(((Boolean) value).booleanValue() ? YES_VALUE : NO_VALUE);
    }
    else {
      cell.setCellValue(value.toString().trim());
    }
    return cell;
  }
  
  private static 
  Map<String, CellStyle> createStyles(Workbook workBook)
  {
    Map<String, CellStyle> mapResult = new HashMap<String, CellStyle>();
    mapResult.put("whiteb",  createStyle(workBook, true,  HorizontalAlignment.CENTER,   false));
    mapResult.put("whitec",  createStyle(workBook, false, HorizontalAlignment.CENTER,   false));
    mapResult.put("whitel",  createStyle(workBook, false, HorizontalAlignment.LEFT,     false));
    mapResult.put("whiter",  createStyle(workBook, false, HorizontalAlignment.RIGHT,    false));
    mapResult.put("whited",  createStyle(workBook, false, HorizontalAlignment.LEFT,     DATE_FORMAT));
    if(HEADER_GRAY) {
      mapResult.put("headerc", createStyle(workBook, true, (short) HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex(), HorizontalAlignment.CENTER, true));
      mapResult.put("headerl", createStyle(workBook, true, (short) HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex(), HorizontalAlignment.LEFT,   true));
      mapResult.put("headerr", createStyle(workBook, true, (short) HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex(), HorizontalAlignment.RIGHT));
    }
    else {
      mapResult.put("headerc", createStyle(workBook, true, HorizontalAlignment.CENTER, true));
      mapResult.put("headerl", createStyle(workBook, true, HorizontalAlignment.LEFT,   true));
      mapResult.put("headerr", createStyle(workBook, true, HorizontalAlignment.RIGHT,  true));
    }
    return mapResult;
  }
  
  private static 
  CellStyle createStyle(Workbook workBook, boolean bold, HorizontalAlignment alignment, boolean boWrapText)
  {
    Font font = workBook.createFont();
    font.setFontHeightInPoints((short) 10);
    font.setFontName("Arial");
    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
    if(bold) font.setBold(bold);
    CellStyle cellStyle = workBook.createCellStyle();
    cellStyle.setFont(font);
    cellStyle.setWrapText(boWrapText);
    cellStyle.setAlignment(alignment);
    if(CELL_BORDER) {
      cellStyle.setBorderBottom(BorderStyle.THIN);
      cellStyle.setBorderTop(BorderStyle.THIN);
      cellStyle.setBorderLeft(BorderStyle.THIN);
      cellStyle.setBorderRight(BorderStyle.THIN);
    }
    return cellStyle;
  }
  
  private static 
  CellStyle createStyle(Workbook workBook, boolean bold, HorizontalAlignment alignment, String dataFormat)
  {
    Font font = workBook.createFont();
    font.setFontHeightInPoints((short) 10);
    font.setFontName("Arial");
    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
    if(bold) font.setBold(bold);
    CellStyle cellStyle = workBook.createCellStyle();
    cellStyle.setFont(font);
    if(dataFormat != null && dataFormat.length() > 0) {
      CreationHelper createHelper = workBook.getCreationHelper();
      cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(dataFormat));
    }
    cellStyle.setAlignment(alignment);
    if(CELL_BORDER) {
      cellStyle.setBorderBottom(BorderStyle.THIN);
      cellStyle.setBorderTop(BorderStyle.THIN);
      cellStyle.setBorderLeft(BorderStyle.THIN);
      cellStyle.setBorderRight(BorderStyle.THIN);
    }
    return cellStyle;
  }
  
  private static 
  CellStyle createStyle(Workbook workBook, boolean bold, short background, HorizontalAlignment alignment)
  {
    Font font = workBook.createFont();
    font.setFontHeightInPoints((short) 10);
    font.setFontName("Arial");
    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
    if(bold) font.setBold(bold);
    CellStyle cellStyle = workBook.createCellStyle();
    cellStyle.setFont(font);
    cellStyle.setWrapText(false);
    cellStyle.setFillForegroundColor((short) background);
    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    cellStyle.setAlignment(alignment);
    if(CELL_BORDER) {
      cellStyle.setBorderBottom(BorderStyle.THIN);
      cellStyle.setBorderTop(BorderStyle.THIN);
      cellStyle.setBorderLeft(BorderStyle.THIN);
      cellStyle.setBorderRight(BorderStyle.THIN);
    }
    return cellStyle;
  }
  
  private static 
  CellStyle createStyle(Workbook workBook, boolean bold, short background, HorizontalAlignment alignment, boolean boWrapText)
  {
    Font font = workBook.createFont();
    font.setFontHeightInPoints((short) 10);
    font.setFontName("Arial");
    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
    if(bold) font.setBold(bold);
    CellStyle cellStyle = workBook.createCellStyle();
    cellStyle.setFont(font);
    cellStyle.setWrapText(boWrapText);
    cellStyle.setFillForegroundColor((short) background);
    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    cellStyle.setAlignment(alignment);
    if(CELL_BORDER) {
      cellStyle.setBorderBottom(BorderStyle.THIN);
      cellStyle.setBorderTop(BorderStyle.THIN);
      cellStyle.setBorderLeft(BorderStyle.THIN);
      cellStyle.setBorderRight(BorderStyle.THIN);
    }
    return cellStyle;
  }
  
  private static
  String csvRow(List<?> items)
  {
    if(items == null || items.size() == 0) {
      return "";
    }
    if(CSV_SEPARATOR == null) CSV_SEPARATOR = "";
    if(CSV_DELIMITER == null) CSV_DELIMITER = "";
    String result = "";
    for(Object item : items) {
      if(item instanceof Date) {
        result += CSV_SEPARATOR + CSV_DELIMITER + formatDate((Date) item) + CSV_DELIMITER;
      }
      else if(item instanceof Calendar) {
        result += CSV_SEPARATOR + CSV_DELIMITER + formatDate((Calendar) item) + CSV_DELIMITER;
      }
      else if(item instanceof Boolean) {
        String yesNo = ((Boolean) item).booleanValue() ? YES_VALUE : NO_VALUE;
        result += CSV_SEPARATOR + CSV_DELIMITER + yesNo + CSV_DELIMITER;
      }
      else if(item instanceof Number) {
        result += CSV_SEPARATOR + CSV_DELIMITER + item.toString().replace('.', ',') + CSV_DELIMITER;
      }
      else if(item != null) {
        result += CSV_SEPARATOR + CSV_DELIMITER + item.toString().replace(';', ',').replace('\n', ' ').replace("\r", "").replace('"', '\'').trim() + CSV_DELIMITER;
      }
      else {
        result += CSV_SEPARATOR + CSV_DELIMITER + CSV_DELIMITER;
      }
    }
    if(result.length() > 0) {
      result = result.substring(CSV_SEPARATOR.length());
    }
    return result;
  }
  
  private static
  String formatDate(Date date)
  {
    if(date == null) return "";
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(date.getTime());
    return formatDate(cal);
  }
  
  private static
  String formatDate(Calendar cal)
  {
    if(cal == null) return "";
    int iYear  = cal.get(Calendar.YEAR);
    int iMonth = cal.get(Calendar.MONTH) + 1;
    int iDay   = cal.get(Calendar.DATE);
    String sYear  = String.valueOf(iYear);
    String sMonth = iMonth < 10 ? "0" + iMonth : String.valueOf(iMonth);
    String sDay   = iDay   < 10 ? "0" + iDay   : String.valueOf(iDay);
    if(iYear < 10) {
      sYear = "000" + sYear;
    }
    else if(iYear < 100) {
      sYear = "00" + sYear;
    }
    else if(iYear < 1000) {
      sYear = "0" + sYear;
    }
    if(DATE_FORMAT == null || DATE_FORMAT.length() == 0 || DATE_FORMAT.startsWith("#")) {
      return sYear + sMonth + sDay;
    }
    else if(DATE_FORMAT.indexOf('-') >= 0 || DATE_FORMAT.startsWith("y")) {
      return sYear + "-" + sMonth + "-" + sDay;
    }
    else if(DATE_FORMAT.startsWith("m") || DATE_FORMAT.startsWith("u")) {
      return sMonth + "/" + sDay + "/" + sYear;
    }
    return sDay + "/" + sMonth + "/" + sYear;
  }
  
  public static
  String getDesktopPath(String sFileName)
  {
    String sUserHome = System.getProperty("user.home");
    return sUserHome + File.separator + "Desktop" + File.separator + sFileName;
  }
  
  public static
  void saveContent(byte[] content, String filePath)
    throws Exception
  {
    if(content  == null || content.length    == 0) return;
    if(filePath == null || filePath.length() == 0) return;
    FileOutputStream fos = null;
    try {
      File file = new File(filePath);
      fos = new FileOutputStream(file);
      fos.write(content);
      System.out.println("File " + file.getAbsolutePath() + " saved.");
    }
    finally {
      if(fos != null) try{ fos.close(); } catch(Exception ex) {}
    }
  }
}
