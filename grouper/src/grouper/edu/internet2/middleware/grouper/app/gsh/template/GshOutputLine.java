package edu.internet2.middleware.grouper.app.gsh.template;


public class GshOutputLine {

  /**
   * success, info, error
   */
  private String messageType;
  
  private String text;
  
  public GshOutputLine(String text) {
    this.text = text;
    this.messageType = "success";
  }
  
  public GshOutputLine(String messageType, String text) {
    this.messageType = messageType;
    this.text = text;
  }
  
  public String getMessageType() {
    return messageType;
  }

  public String getText() {
    return text;
  }
  
  
}
