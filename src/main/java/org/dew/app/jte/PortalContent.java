package org.dew.app.jte;

import gg.jte.Content;
import gg.jte.TemplateOutput;

public class PortalContent implements Content {
  
  protected StringBuilder body;
  
  public PortalContent() {
    this.body = new StringBuilder();
  }
  
  public PortalContent(Object... rows) {
    this.body = new StringBuilder();
    this.add(rows);
  }
  
  public String getBody() {
    return this.body.toString();
  }
  
  public PortalContent add(Object... rows) {
    if(rows.length == 0) {
      return this; 
    }
    for(int i = 0; i < rows.length; i++) {
      Object row = rows[i];
      if(row == null) continue;
      body.append(row);
    }
    return this;
  }
  
  @Override
  public void writeTo(TemplateOutput output) {
    output.writeContent(body.toString());
  }
}
