package org.dew.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class AppLogController {
  
  @GetMapping("/list")
  public ResponseEntity<List<String>> findTipiCompetenza(){
    List<String> result = new ArrayList<>();
    File folder = new File(".");
    File[] afFiles = folder.listFiles();
    for (int i = 0; i < afFiles.length; i++) {
      File file = afFiles[i];
      if (file.isFile()) {
        String fileName = file.getName();
        if(fileName.endsWith(".log")) {
          result.add(file.getName());
        }
      }
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/read")
  public ResponseEntity<List<String>> findByFilters(
      @RequestParam(name = "name", required = true) String name,
      @RequestParam(name = "first", required = false, defaultValue = "0") int first,
      @RequestParam(name = "last", required = false, defaultValue = "0") int last,
      @RequestParam(name = "search", required = false) String search
    ) throws Exception {
    if(search == null) search = "";
    File file = new File("./" + name);
    List<String> result = new ArrayList<>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String sLine = null;
      while((sLine = br.readLine()) != null) {
        if(!search.isEmpty()) {
          if(!sLine.contains(search)) {
            continue;
          }
        }
        result.add(sLine);
      }
    }
    finally {
      if(br != null) {
        try {
          br.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    if(first > 0 || last > 0) {
      List<String> result2 = new ArrayList<>(first + last + 2);
      if(first > 0) {
        result2.add("First " + first + " lines:");
        for(int i = 0; i < first; i++) {
          if(result.size() > i) {
            result2.add(result.get(i));
          }
        }
      }
      if(last > 0) {
        result2.add("Last " + last + " lines:");
        for(int i = 0; i < last; i++) {
          int j = result.size() - last + i;
          if(j >= 0) {
            result2.add(result.get(j));
          }
        }
      }
      return new ResponseEntity<>(result2, HttpStatus.OK);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
