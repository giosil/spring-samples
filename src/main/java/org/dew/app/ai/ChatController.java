package org.dew.app.ai;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/chat")
public class ChatController {
  
  protected final static String URL_ENGINE         = "http://localhost:12434/engines/v1";
  protected final static String DEF_MODEL          = "ai/llama3.2:1B-Q4_0";
  protected final static int    TIME_OUT_PARTIAL   = 10;
  protected final static int    TIME_OUT_COMPLETED = 60;
  
  @PostMapping("/completions")
  public void completions(@RequestBody ChatDTO chatDTO, HttpServletResponse response) throws Exception {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/plain");
    
    PrintWriter writer = response.getWriter();
    
    if(chatDTO == null) {
      writer.println("Messaggio non specificato");
      writer.flush();
      return;
    }
    String modelName = chatDTO.getModel();
    if(modelName == null || modelName.length() == 0) {
      modelName = DEF_MODEL;
    }
    List<ChatMessageDTO> messages = chatDTO.getMessages();
    if(messages == null || messages.size() == 0) {
      writer.println("Lista messaggi assente.");
      writer.flush();
      return;
    }
    
    List<ChatMessage> listMessages = new ArrayList<ChatMessage>();
    for(int i = 0; i < messages.size(); i++) {
      ChatMessageDTO messageDTO = messages.get(i);
      if(messageDTO == null) continue;
      String text = messageDTO.getContent();
      if(text == null || text.length() == 0) {
        continue;
      }
      String role = messageDTO.getRole();
      if(role != null && role.equals("system")) {
        listMessages.add(SystemMessage.from(text));
        log("system: " + text);
      }
      else if(role != null && role.equals("assistant")) {
        listMessages.add(AiMessage.from(text));
        log("assistant: " + text);
      }
      else {
        listMessages.add(UserMessage.from(text));
        log("user: " + text);
      }
    }
    
    if(listMessages.size() == 0) {
      writer.println("Prompt non specificato");
      writer.flush();
      return;
    }
    Integer maxTokens   = chatDTO.getMax_tokens();
    Double  temperature = chatDTO.getTemperature();
    Double  top_P       = chatDTO.getTop_p();
    Double  freqPenalty = chatDTO.getFrequency_penalty();
    Double  presPenalty = chatDTO.getPresence_penalty();
    
    log("maxTokens=" + maxTokens + ",temperature=" + temperature + ",top_P=" + top_P + ",freqPenalty=" + freqPenalty + ",presPenalty=" + presPenalty);
    
    // Status control 
    chatDTO.setStopped(false);
    chatDTO.setCompleted(false);
    chatDTO.setLastPartial(System.currentTimeMillis());
    
    if(chatDTO.isStream()) {
      OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
          .baseUrl(URL_ENGINE)
          .modelName(modelName)
          .timeout(Duration.ofSeconds(TIME_OUT_COMPLETED));
      
      if(maxTokens != null) {
        builder.maxTokens(maxTokens);
      }
      if(temperature != null) {
        builder.temperature(temperature);
      }
      if(top_P != null) {
        builder.topP(top_P);
      }
      if(freqPenalty != null) {
        builder.frequencyPenalty(freqPenalty);
      }
      if(presPenalty != null) {
        builder.presencePenalty(presPenalty);
      }
      
      OpenAiStreamingChatModel model = builder.build();
      
      model.chat(listMessages, new StreamingChatResponseHandler() {
        @Override
        public void onPartialResponse(String partialResponse) {
          if(chatDTO.isStopped()) return;
          chatDTO.setLastPartial(System.currentTimeMillis());
          try {
            writer.write(partialResponse);
            writer.flush();
          } 
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
          if(completeResponse != null) {
            log("onCompleteResponse(" + completeResponse.metadata() + ")...");
          }
          else {
            log("onCompleteResponse(" + completeResponse + ")...");
          }
          if(chatDTO.isStopped()) return;
          try {
            writer.write("\n");
            writer.flush();
          } 
          catch (Exception ex) {
            ex.printStackTrace();
          }
          chatDTO.setCompleted(true);
        }
        @Override
        public void onError(Throwable error) {
          log("onError(" + error + ")...");
          chatDTO.setStopped(true);
          try {
            writer.write("\n");
            writer.write("Errore: " + error);
            writer.flush();
          } 
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      });
      String message = "";
      try {
        while(true) {
          Thread.sleep(500);
          if(chatDTO.isCompleted()) {
            break;
          }
          if(chatDTO.isStopped()) {
            break;
          }
          long timeOut = chatDTO.getLastPartial() + TIME_OUT_PARTIAL * 1000;
          long currentTimeMillis = System.currentTimeMillis();
          if(currentTimeMillis > timeOut) {
            chatDTO.setStopped(true);
            message = "Timeout raggiunto.";
            break;
          }
        }
      } 
      catch (Exception ex) {
        ex.printStackTrace();
      }
      if(message != null && message.length() > 0) {
        writer.write("\n" + message + "\n");
        writer.flush();
      }
    }
    else {
      OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
          .baseUrl(URL_ENGINE)
          .modelName(modelName)
          .timeout(Duration.ofSeconds(TIME_OUT_COMPLETED));
      
      if(maxTokens != null) {
        builder.maxTokens(maxTokens);
      }
      if(temperature != null) {
        builder.temperature(temperature);
      }
      if(top_P != null) {
        builder.topP(top_P);
      }
      if(freqPenalty != null) {
        builder.frequencyPenalty(freqPenalty);
      }
      if(presPenalty != null) {
        builder.presencePenalty(presPenalty);
      }
      
      OpenAiChatModel model = builder.build();
      
      // String answer = model.chat(prompt);
      
      ChatResponse chatResponse = model.chat(listMessages);
      String answer = "Nessuna risposta.";
      if(chatResponse != null) {
        AiMessage aiMessage = chatResponse.aiMessage();
        if(aiMessage != null) {
          answer = aiMessage.toString();
        }
      }
      
      writer.write("\n" + answer + "\n");
      writer.flush();
    }
  }
  
  public static void log(String message) {
    Calendar cal = Calendar.getInstance();
    int iYear  = cal.get(Calendar.YEAR);
    int iMonth = cal.get(Calendar.MONTH) + 1;
    int iDay   = cal.get(Calendar.DATE);
    int iHour  = cal.get(Calendar.HOUR_OF_DAY);
    int iMin   = cal.get(Calendar.MINUTE);
    int iSec   = cal.get(Calendar.SECOND);
    String sYear  = String.valueOf(iYear);
    String sMonth = iMonth < 10 ? "0" + iMonth : String.valueOf(iMonth);
    String sDay   = iDay   < 10 ? "0" + iDay   : String.valueOf(iDay);
    String sHour  = iHour  < 10 ? "0" + iHour  : String.valueOf(iHour);
    String sMin   = iMin   < 10 ? "0" + iMin   : String.valueOf(iMin);
    String sSec   = iSec   < 10 ? "0" + iSec   : String.valueOf(iSec);
    System.out.println(sYear + "-" + sMonth + "-" + sDay + " " + sHour + ":" + sMin + ":" + sSec + " [ChatController] " + message);
  }
}
