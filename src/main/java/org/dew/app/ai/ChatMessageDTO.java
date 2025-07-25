package org.dew.app.ai;

import java.io.Serializable;

public class ChatMessageDTO implements Serializable {
  
  private static final long serialVersionUID = -2543763424909228239L;
  
  private String role;
  private String content;
  
  public ChatMessageDTO()
  {
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
  
  @Override
  public boolean equals(Object object) {
    if(object instanceof ChatMessageDTO) {
      String objRole    = ((ChatMessageDTO) object).getRole();
      String objContent = ((ChatMessageDTO) object).getContent();
      return (role + "$" + content).equals(objRole + "$" + objContent);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return (role + "$" + content).hashCode();
  }
  
  @Override
  public String toString() {
    if(content == null) return "";
    return content;
  }
}
