package org.dew.app.ai;

import java.io.Serializable;
import java.util.List;

public class ChatDTO implements Serializable{
  
  private static final long serialVersionUID = -1216323646691194594L;
  
  private String  model;
  private List<ChatMessageDTO> messages;
  private boolean stream;
  private Integer max_tokens;
  private Double  temperature;
  private Double  top_p;
  private Double  frequency_penalty;
  private Double  presence_penalty;
  // Extra 
  private long    lastPartial;
  private boolean stopped;
  private boolean completed;
  
  public ChatDTO()
  {
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public List<ChatMessageDTO> getMessages() {
    return messages;
  }

  public void setMessages(List<ChatMessageDTO> messages) {
    this.messages = messages;
  }

  public boolean isStream() {
    return stream;
  }

  public void setStream(boolean stream) {
    this.stream = stream;
  }

  public Integer getMax_tokens() {
    return max_tokens;
  }

  public void setMax_tokens(Integer max_tokens) {
    this.max_tokens = max_tokens;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Double getTop_p() {
    return top_p;
  }

  public void setTop_p(Double top_p) {
    this.top_p = top_p;
  }

  public Double getFrequency_penalty() {
    return frequency_penalty;
  }

  public void setFrequency_penalty(Double frequency_penalty) {
    this.frequency_penalty = frequency_penalty;
  }

  public Double getPresence_penalty() {
    return presence_penalty;
  }

  public void setPresence_penalty(Double presence_penalty) {
    this.presence_penalty = presence_penalty;
  }

  public long getLastPartial() {
    return lastPartial;
  }

  public void setLastPartial(long lastPartial) {
    this.lastPartial = lastPartial;
  }

  public boolean isStopped() {
    return stopped;
  }

  public void setStopped(boolean stopped) {
    this.stopped = stopped;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  @Override
  public boolean equals(Object object) {
    if(object instanceof ChatDTO) {
      ChatDTO objChatDTO = (ChatDTO) object;
      return (model + "$" + messages).equals(objChatDTO.getModel() + "$" + objChatDTO.getMessages());
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return (model + "$" + messages).hashCode();
  }
  
  @Override
  public String toString() {
    if(model == null) return "";
    if(messages != null) {
      return model + " [" + messages.size() + "]";
    }
    return model;
  }
}
